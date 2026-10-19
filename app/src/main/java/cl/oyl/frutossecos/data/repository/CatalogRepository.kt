package cl.oyl.frutossecos.data.repository

import cl.oyl.frutossecos.data.local.*
import cl.oyl.frutossecos.data.remote.OylNetwork
import kotlinx.coroutines.flow.Flow

/** Datos sintéticos: no existe todavía disponibilidad oficial de servidor. */
class CatalogRepository(private val dao: CatalogDao) {
 val products: Flow<List<ProductEntity>> = dao.products()
 fun cart(alias: String) = dao.cartRaw(alias)
 suspend fun seedDemoProducts() {
  if (dao.productCount() > 0) return
  dao.saveProducts(listOf(
   ProductEntity("OY001", "Almendras naturales", "Frutos secos", "Almendras crujientes sin aditivos", "250 g", 2990, 25),
   ProductEntity("OY002", "Nueces", "Frutos secos", "Nueces en mitades", "250 g", 3490, 18),
   ProductEntity("OY003", "Mix premium", "Frutos secos", "Mezcla de nueces y almendras", "250 g", 3990, 40),
   ProductEntity("OY004", "Pasas morenas", "Deshidratados", "Pasas de uva", "250 g", 2490, 36),
   ProductEntity("OY005", "Semillas de chía", "Semillas", "Semillas naturales", "250 g", 2290, 12),
   ProductEntity("OY006", "Cranberries", "Deshidratados", "Arándanos rojos deshidratados", "250 g", 2890, 16)
  ))
 }
 suspend fun syncFromServer(): Boolean = try {
  val remote = OylNetwork.api.products()
  dao.saveProducts(remote.map {
   ProductEntity(it.id,it.name,it.category,it.description ?: "",it.presentation ?: "",it.price,it.stock,it.active)
  })
  true
 } catch (_: Exception) { false }
 suspend fun clearCart(owner:String) = dao.clearCart(owner)
 suspend fun changeCart(owner: String, productId: String, delta: Int) {
  val product = dao.getProduct(productId) ?: return
  val current = dao.getCartItem(owner, productId)?.quantity ?: 0
  val target = (current + delta).coerceIn(0, product.stock)
  if (target == 0) dao.removeCart(owner, productId)
  else dao.upsertCart(CartItemEntity(owner, productId, target))
 }
}