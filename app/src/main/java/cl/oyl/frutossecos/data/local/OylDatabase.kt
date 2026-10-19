package cl.oyl.frutossecos.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "clients", indices = [Index(value = ["email"], unique = true)])
data class ClientEntity(@PrimaryKey val alias: String, val name: String, val email: String,
 val phone: String, val address: String, val salt: String, val hash: String)

@Dao interface ClientDao {
 @Query("SELECT * FROM clients WHERE alias = :alias LIMIT 1") suspend fun byAlias(alias: String): ClientEntity?
 @Query("SELECT * FROM clients WHERE email = :email LIMIT 1") suspend fun byEmail(email: String): ClientEntity?
 @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun add(client: ClientEntity)
 @Query("UPDATE clients SET name=:name, phone=:phone, address=:address WHERE alias=:alias")
 suspend fun updateProfile(alias: String, name: String, phone: String, address: String)
}

@Entity(tableName = "products")
data class ProductEntity(@PrimaryKey val id: String, val name: String, val category: String,
 val description: String, val presentation: String, val price: Int, val stock: Int,
 val active: Boolean = true)

@Entity(tableName = "cart_items", primaryKeys = ["ownerAlias", "productId"],
 foreignKeys = [ForeignKey(entity = ProductEntity::class, parentColumns = ["id"],
  childColumns = ["productId"], onDelete = ForeignKey.CASCADE)])
data class CartItemEntity(val ownerAlias: String, val productId: String, val quantity: Int)

data class CartLine(
 @Embedded val item: CartItemEntity,
 @Relation(parentColumn = "productId", entityColumn = "id") val product: ProductEntity
)

@Dao interface CatalogDao {
 @Query("SELECT * FROM products WHERE active = 1 ORDER BY name") fun products(): Flow<List<ProductEntity>>
 @Query("SELECT * FROM products WHERE id = :id LIMIT 1") suspend fun getProduct(id: String): ProductEntity?
 @Query("SELECT COUNT(*) FROM products") suspend fun productCount(): Int
 @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveProducts(values: List<ProductEntity>)
 @Query("SELECT * FROM cart_items WHERE ownerAlias = :alias ORDER BY productId")
 fun cartRaw(alias: String): Flow<List<CartItemEntity>>
 @Query("SELECT * FROM cart_items WHERE ownerAlias = :alias AND productId = :product LIMIT 1")
 suspend fun getCartItem(alias: String, product: String): CartItemEntity?
 @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertCart(value: CartItemEntity)
 @Query("DELETE FROM cart_items WHERE ownerAlias = :alias") suspend fun clearCart(alias: String)
 @Query("DELETE FROM cart_items WHERE ownerAlias = :alias AND productId = :product")
 suspend fun removeCart(alias: String, product: String)
}

@Database(entities = [ClientEntity::class, ProductEntity::class, CartItemEntity::class], version = 1, exportSchema = false)
abstract class OylDatabase: RoomDatabase() {
 abstract fun clientDao(): ClientDao
 abstract fun catalogDao(): CatalogDao
}