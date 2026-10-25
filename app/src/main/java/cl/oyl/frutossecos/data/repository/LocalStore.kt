package cl.oyl.frutossecos.data.repository

import androidx.room.withTransaction
import cl.oyl.frutossecos.data.local.LocalOrderEntity
import cl.oyl.frutossecos.data.local.OylDatabase
import cl.oyl.frutossecos.data.local.ProductEntity
import cl.oyl.frutossecos.data.model.*
import com.google.gson.Gson
import java.time.Instant
import java.util.UUID

object LocalStore {

    lateinit var repository: LocalStoreRepository
        private set

    fun initialize(database: OylDatabase) {
        repository = LocalStoreRepository(database)
    }
}

class LocalStoreRepository(
    private val database: OylDatabase
) {
    private val gson = Gson()

    private val catalog
        get() = database.catalogDao()

    private val ordersDao
        get() = database.orderDao()

    suspend fun products(): List<LocalProduct> {
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
        return database.withTransaction {
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

            val existing = catalog.getProduct(value.id)

            val saved = ProductEntity(
                id = value.id,
                name = value.name,
                category = value.category,
                description = value.description,
                presentation = value.presentation,
                price = value.price,
                // Editar los datos no reemplaza el stock actual.
                stock = existing?.stock ?: value.stock,
                active = value.active
            )

            catalog.saveProducts(listOf(saved))

            saved.toModel()
        }
    }

    suspend fun adjustStock(
        id: String,
        value: StockDeltaDto
    ): LocalProduct {
        return database.withTransaction {
            val product = catalog.getProduct(id)
                ?: error("Producto no encontrado.")

            val newStock =
                product.stock.toLong() + value.delta.toLong()

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

    suspend fun allOrders(): List<LocalOrder> {
        return ordersDao.all().map {
            it.toModel()
        }
    }

    suspend fun orders(alias: String): List<LocalOrder> {
        return ordersDao.byOwner(alias).map {
            it.toModel()
        }
    }

    suspend fun setStatus(
        id: String,
        value: OrderStatusDto
    ): LocalOrder {
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
            active = active
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