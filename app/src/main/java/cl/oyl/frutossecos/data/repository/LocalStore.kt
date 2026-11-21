package cl.oyl.frutossecos.data.repository

import androidx.room.withTransaction
import cl.oyl.frutossecos.data.local.LocalOrderEntity
import cl.oyl.frutossecos.data.local.OylDatabase
import cl.oyl.frutossecos.data.local.ProductEntity
import cl.oyl.frutossecos.data.model.*
import cl.oyl.frutossecos.data.remote.ApiException
import cl.oyl.frutossecos.data.remote.RemoteRepository
import cl.oyl.frutossecos.data.remote.ServerUnavailableException
import com.google.gson.Gson
import java.time.Instant
import java.util.UUID

object LocalStore {

    lateinit var repository: LocalStoreRepository
        private set

    fun initialize(
        database: OylDatabase,
        remote: RemoteRepository = RemoteRepository()
    ) {
        repository = LocalStoreRepository(database, remote)
    }
}

/**
 * Repositorio de la tienda: los microservicios son la fuente de verdad y Room guarda una copia local.
 * - Lecturas: se consulta el servidor y se actualiza Room; si no hay conexión se usa Room.
 * - Escrituras: se envían primero al servidor; si está apagado, se aplican solo en Room.
 * - Si el servidor rechaza la operación (stock insuficiente, datos inválidos), se muestra su mensaje.
 */
class LocalStoreRepository(
    private val database: OylDatabase,
    private val remote: RemoteRepository = RemoteRepository()
) {
    private val gson = Gson()

    // Evita esperar timeouts en cada llamada cuando el servidor está apagado.
    private var downUntil = 0L
    private var nextCatalogSyncAt = 0L

    private suspend fun <T> remoteCall(block: suspend () -> Result<T>): Result<T> {
        if (System.currentTimeMillis() < downUntil) {
            return Result.failure(ServerUnavailableException("Servidor no disponible"))
        }
        val result = block()
        if (result.exceptionOrNull() is ServerUnavailableException) {
            downUntil = System.currentTimeMillis() + 15_000
        }
        return result
    }

    /** El servidor rechazó la operación: se propaga su mensaje. Si no hay conexión, devuelve null. */
    private fun <T> Result<T>.rejectionOrNull(): Exception? {
        val failure = exceptionOrNull() ?: return null
        return if (failure is ServerUnavailableException) null else IllegalArgumentException(failure.message)
    }

    private suspend fun syncProducts() {
        if (System.currentTimeMillis() < nextCatalogSyncAt) return
        val list = remoteCall { remote.products() }.getOrNull() ?: return
        nextCatalogSyncAt = System.currentTimeMillis() + 3_000
        catalog.saveProducts(list.map { it.toEntity() })
    }

    private val catalog
        get() = database.catalogDao()

    private val ordersDao
        get() = database.orderDao()

    suspend fun products(): List<LocalProduct> {
        syncProducts()

        return database.withTransaction {
            CatalogRepository(catalog).seedDemoProducts()

            catalog.productList().map {
                it.toModel()
            }
        }
    }

    suspend fun saveProduct(
        value: ProductInputDto
    ): LocalProduct {
        require(
            value.id.isNotBlank() &&
                    value.name.isNotBlank()
        ) {
            "Identificador y nombre obligatorios."
        }

        require(
            value.category.isNotBlank() &&
                    value.description.isNotBlank() &&
                    value.presentation.isNotBlank()
        ) {
            "Completa categoría, descripción y presentación."
        }

        require(value.price > 0) {
            "El precio debe ser mayor que cero."
        }

        require(value.stock >= 0) {
            "El stock no puede ser negativo."
        }

        val imageUrl = value.imageUrl?.trim()?.takeIf { it.isNotEmpty() }
        imageUrl?.let {
            require(it.startsWith("https://")) {
                "La URL de la imagen debe comenzar con https://."
            }
        }

        // Primero el microservicio (crea o edita).
        val remoteResult = remoteCall { remote.saveProduct(value.copy(imageUrl = imageUrl)) }
        remoteResult.rejectionOrNull()?.let { throw it }
        val serverProduct = remoteResult.getOrNull()

        return database.withTransaction {
            val existing = catalog.getProduct(value.id)

            val saved = ProductEntity(
                id = value.id,
                name = value.name,
                category = value.category,
                description = value.description,
                presentation = value.presentation,
                price = value.price,
                // Editar los datos no reemplaza el stock actual.
                stock = serverProduct?.stock ?: existing?.stock ?: value.stock,
                active = value.active,
                imageUrl = imageUrl
            )

            catalog.saveProducts(listOf(saved))

            saved.toModel()
        }
    }

    suspend fun adjustStock(
        id: String,
        value: StockDeltaDto
    ): LocalProduct {
        val remoteResult = remoteCall { remote.adjustStock(id, value.delta) }
        remoteResult.rejectionOrNull()?.let { throw it }
        val serverProduct = remoteResult.getOrNull()

        return database.withTransaction {
            val product = catalog.getProduct(id)
                ?: error("Producto no encontrado.")

            val newStock = serverProduct?.stock?.toLong()
                ?: (product.stock.toLong() + value.delta.toLong())

            require(newStock in 0..Int.MAX_VALUE.toLong()) {
                "El movimiento produce un stock inválido."
            }

            val saved = product.copy(
                stock = newStock.toInt()
            )

            catalog.saveProducts(listOf(saved))

            saved.toModel()
        }
    }

    suspend fun order(
        request: LocalOrderRequest
    ): LocalOrder {
        require(request.clientAlias.isNotBlank()) {
            "Inicia sesión para comprar."
        }

        require(request.items.isNotEmpty()) {
            "El carrito está vacío."
        }

        val remoteResult = remoteCall { remote.placeOrder(request) }
        remoteResult.rejectionOrNull()?.let { throw it }

        val serverOrder = remoteResult.getOrNull()
        return if (serverOrder != null) {
            saveServerOrder(serverOrder)
        } else {
            // Sin conexión: se registra solo en Room.
            localOrder(request)
        }
    }

    private suspend fun saveServerOrder(order: LocalOrder): LocalOrder {
        return database.withTransaction {
            order.lines.forEach { line ->
                catalog.getProduct(line.productId)?.let { product ->
                    catalog.saveProducts(
                        listOf(
                            product.copy(
                                stock = (product.stock - line.quantity).coerceAtLeast(0)
                            )
                        )
                    )
                }
            }

            ordersDao.save(order.toEntity())
            catalog.clearCart(order.clientAlias)

            order
        }
    }

    private suspend fun localOrder(
        request: LocalOrderRequest
    ): LocalOrder {
        return database.withTransaction {
            require(request.clientAlias.isNotBlank()) {
                "Inicia sesión para comprar."
            }

            require(request.items.isNotEmpty()) {
                "El carrito está vacío."
            }

            val quantities = linkedMapOf<String, Long>()

            request.items.forEach { item ->
                require(item.quantity > 0) {
                    "La cantidad debe ser mayor que cero."
                }

                quantities[item.productId] =
                    (quantities[item.productId] ?: 0L) +
                            item.quantity.toLong()
            }

            var total = 0L

            val lines = quantities.entries.mapIndexed {
                    index, entry ->

                val productId = entry.key
                val quantity = entry.value

                val product = catalog.getProduct(productId)
                    ?: error("Producto $productId no encontrado.")

                require(product.active) {
                    "${product.name} ya no está disponible."
                }

                require(quantity <= product.stock.toLong()) {
                    "Stock insuficiente para ${product.name}."
                }

                require(product.price > 0) {
                    "Precio inválido para ${product.name}."
                }

                total += product.price.toLong() * quantity

                require(total <= Int.MAX_VALUE.toLong()) {
                    "El total supera el máximo permitido."
                }

                catalog.saveProducts(
                    listOf(
                        product.copy(
                            stock = product.stock - quantity.toInt()
                        )
                    )
                )

                LocalOrderLine(
                    id = index.toLong(),
                    productId = product.id,
                    productName = product.name,
                    quantity = quantity.toInt(),
                    unitPrice = product.price
                )
            }

            val order = LocalOrder(
                id = UUID.randomUUID().toString(),
                clientAlias = request.clientAlias,
                createdAt = Instant.now().toString(),
                total = total.toInt(),
                status = "Pendiente de Validación",
                fulfillment = request.fulfillment,
                lines = lines
            )

            ordersDao.save(order.toEntity())

            catalog.clearCart(request.clientAlias)

            order
        }
    }

    /** Elimina el producto en catalog-service y en Room. Requiere conexión con el servidor. */
    suspend fun deleteProduct(id: String) {
        val failure = remoteCall { remote.deleteProduct(id) }.exceptionOrNull()

        // 404: el servidor ya no lo tenía; basta con quitarlo de Room.
        if (failure != null && !(failure is ApiException && failure.code == 404)) {
            throw IllegalArgumentException(failure.message)
        }

        catalog.deleteProduct(id)
    }

    /** Cancela un pedido pendiente: el servidor repone el stock y aquí se quita de Room. */
    suspend fun cancelOrder(id: String) {
        val current = ordersDao.byId(id)?.toModel()
            ?: error("Pedido no encontrado.")

        require(current.status == "Pendiente de Validación") {
            "Solo se puede cancelar un pedido pendiente."
        }

        val failure = remoteCall { remote.cancelOrder(id) }.exceptionOrNull()
        if (failure is ApiException && failure.code != 404) {
            throw IllegalArgumentException(failure.message)
        }

        database.withTransaction {
            current.lines.forEach { line ->
                catalog.getProduct(line.productId)?.let { product ->
                    catalog.saveProducts(
                        listOf(product.copy(stock = product.stock + line.quantity))
                    )
                }
            }

            ordersDao.delete(id)
        }
    }

    suspend fun allOrders(): List<LocalOrder> {
        remoteCall { remote.allOrders() }.getOrNull()?.let { list ->
            cacheOrders(list)
            return list
        }

        return ordersDao.all().map {
            it.toModel()
        }
    }

    suspend fun orders(alias: String): List<LocalOrder> {
        remoteCall { remote.ordersOf(alias) }.getOrNull()?.let { list ->
            cacheOrders(list)
            return list
        }

        return ordersDao.byOwner(alias).map {
            it.toModel()
        }
    }

    private suspend fun cacheOrders(list: List<LocalOrder>) {
        list.forEach { ordersDao.save(it.toEntity()) }
    }

    suspend fun setStatus(
        id: String,
        value: OrderStatusDto
    ): LocalOrder {
        val remoteResult = remoteCall { remote.setOrderStatus(id, value.status) }
        remoteResult.rejectionOrNull()?.let { throw it }

        remoteResult.getOrNull()?.let { updated ->
            ordersDao.save(updated.toEntity())
            return updated
        }

        return database.withTransaction {
            val current = ordersDao.byId(id)?.toModel()
                ?: error("Pedido no encontrado.")

            val nextStatus = when (current.status) {
                "Pendiente de Validación" -> "Confirmado"
                "Confirmado" -> "Preparado"
                "Preparado" -> "Entregado"
                else -> null
            }

            require(
                value.status == current.status ||
                        value.status == nextStatus
            ) {
                "Cambio de estado no permitido."
            }

            val updated = current.copy(
                status = value.status
            )

            ordersDao.save(updated.toEntity())

            updated
        }
    }

    private fun ProductEntity.toModel(): LocalProduct {
        return LocalProduct(
            id = id,
            name = name,
            category = category,
            description = description,
            presentation = presentation,
            price = price,
            stock = stock,
            active = active,
            imageUrl = imageUrl
        )
    }

    private fun LocalProduct.toEntity(): ProductEntity {
        return ProductEntity(
            id = id,
            name = name,
            category = category,
            description = description.orEmpty(),
            presentation = presentation.orEmpty(),
            price = price,
            stock = stock,
            active = active,
            imageUrl = imageUrl
        )
    }

    private fun LocalOrder.toEntity(): LocalOrderEntity {
        return LocalOrderEntity(
            id = id,
            clientAlias = clientAlias,
            createdAt = createdAt,
            payload = gson.toJson(this)
        )
    }

    private fun LocalOrderEntity.toModel(): LocalOrder {
        return gson.fromJson(
            payload,
            LocalOrder::class.java
        )
    }
}