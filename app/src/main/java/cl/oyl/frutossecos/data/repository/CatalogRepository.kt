package cl.oyl.frutossecos.data.repository

import cl.oyl.frutossecos.data.local.CartItemEntity
import cl.oyl.frutossecos.data.local.CatalogDao
import cl.oyl.frutossecos.data.local.ProductEntity
import cl.oyl.frutossecos.data.model.StockDeltaDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class CatalogRepository(
 private val dao: CatalogDao
) {
 val products: Flow<List<ProductEntity>> = dao.products()

 fun cart(alias: String) = dao.cartRaw(alias)

 suspend fun seedDemoProducts() {
  if (dao.productCount() > 0) return

  dao.seedProducts(
   listOf(
    ProductEntity(
     id = "OY001",
     name = "Almendras naturales",
     category = "Frutos secos",
     description = "Almendras naturales seleccionadas, ideales para desayunos y colaciones.",
     presentation = "250 g",
     price = 2990,
     stock = 25
    ),
    ProductEntity(
     id = "OY002",
     name = "Nueces",
     category = "Frutos secos",
     description = "Nueces seleccionadas para acompañar desayunos, ensaladas y colaciones.",
     presentation = "250 g",
     price = 3490,
     stock = 18
    ),
    ProductEntity(
     id = "OY003",
     name = "Mix premium",
     category = "Frutos secos",
     description = "Selección de frutos secos con distintas texturas y sabores.",
     presentation = "250 g",
     price = 3990,
     stock = 40
    ),
    ProductEntity(
     id = "OY004",
     name = "Pasas morenas",
     category = "Deshidratados",
     description = "Pasas morenas para colaciones, desayunos y preparaciones.",
     presentation = "250 g",
     price = 2490,
     stock = 36
    ),
    ProductEntity(
     id = "OY005",
     name = "Semillas de chía",
     category = "Semillas",
     description = "Semillas de chía para incorporar a yogur, batidos y postres.",
     presentation = "250 g",
     price = 2290,
     stock = 12
    ),
    ProductEntity(
     id = "OY006",
     name = "Cranberries",
     category = "Deshidratados",
     description = "Cranberries deshidratados de sabor agridulce para ensaladas y colaciones.",
     presentation = "250 g",
     price = 2890,
     stock = 16
    )
   )
  )
 }

 suspend fun refreshLocalCatalog(): Boolean {
  return try {
   seedDemoProducts()
   true
  } catch (e: CancellationException) {
   throw e
  } catch (_: Exception) {
   false
  }
 }

 // Compatibilidad temporal con las pantallas que aún usan este nombre.
 // No realiza ninguna conexión ni consulta a un servidor.
 suspend fun syncFromServer(): Boolean {
  return refreshLocalCatalog()
 }

 suspend fun clearCart(owner: String) {
  dao.clearCart(owner)
 }

 suspend fun adjustStock(productId: String, delta: Int): Boolean {
  val p = dao.getProduct(productId) ?: return false
  val target = (p.stock + delta).coerceAtLeast(0)
  if (target == p.stock) return false
  dao.saveProducts(listOf(p.copy(stock = target)))
  return true
 }

 suspend fun upsertProduct(product: ProductEntity) = dao.saveProducts(listOf(product))

 suspend fun changeCart(
  owner: String,
  productId: String,
  delta: Int
 ) {
  val product = dao.getProduct(productId) ?: return

  if (!product.active && delta > 0) return

  val current = dao.getCartItem(
   owner,
   productId
  )?.quantity ?: 0

  val target = (
          current.toLong() + delta.toLong()
          ).coerceIn(
    0L,
    product.stock.coerceAtLeast(0).toLong()
   ).toInt()

  if (target == 0) {
   dao.removeCart(owner, productId)
  } else {
   dao.upsertCart(
    CartItemEntity(
     ownerAlias = owner,
     productId = productId,
     quantity = target
    )
   )
  }
 }
}