package cl.oyl.frutossecos.data.local

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(
 tableName = "clients",
 indices = [
  Index(value = ["email"], unique = true)
 ]
)
data class ClientEntity(
 @PrimaryKey val alias: String,
 val name: String,
 val email: String,
 val phone: String,
 val address: String,
 val salt: String,
 val hash: String
)

@Dao
interface ClientDao {

 @Query("SELECT * FROM clients WHERE alias = :alias LIMIT 1")
 suspend fun byAlias(alias: String): ClientEntity?

 @Query("SELECT * FROM clients WHERE email = :email LIMIT 1")
 suspend fun byEmail(email: String): ClientEntity?

 @Insert(onConflict = OnConflictStrategy.ABORT)
 suspend fun add(client: ClientEntity)

 @Query(
  """
        UPDATE clients
        SET name = :name,
            phone = :phone,
            address = :address
        WHERE alias = :alias
        """
 )
 suspend fun updateProfile(
  alias: String,
  name: String,
  phone: String,
  address: String
 )
 @Query("UPDATE clients SET name = :name, email = :email, phone = :phone WHERE alias = :alias")
 suspend fun updateContact(alias: String, name: String, email: String, phone: String)
}


@Entity(tableName = "products")
data class ProductEntity(
 @PrimaryKey val id: String,
 val name: String,
 val category: String,
 val description: String,
 val presentation: String,
 val price: Int,
 val stock: Int,
 val active: Boolean = true
)

@Entity(
 tableName = "cart_items",
 primaryKeys = ["ownerAlias", "productId"],
 foreignKeys = [
  ForeignKey(
   entity = ProductEntity::class,
   parentColumns = ["id"],
   childColumns = ["productId"],
   onDelete = ForeignKey.CASCADE
  )
 ]
)
data class CartItemEntity(
 val ownerAlias: String,
 val productId: String,
 val quantity: Int
)

data class CartLine(
 @Embedded val item: CartItemEntity,
 @Relation(
  parentColumn = "productId",
  entityColumn = "id"
 )
 val product: ProductEntity
)

@Dao
interface CatalogDao {

 @Query("SELECT * FROM products WHERE active = 1 ORDER BY name")
 fun products(): Flow<List<ProductEntity>>

 @Query("SELECT * FROM products ORDER BY name")
 fun allProducts(): Flow<List<ProductEntity>>

 @Query("SELECT * FROM products ORDER BY name")
 suspend fun productList(): List<ProductEntity>

 @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
 suspend fun getProduct(id: String): ProductEntity?

 @Query("SELECT COUNT(*) FROM products")
 suspend fun productCount(): Int

 @Insert(onConflict = OnConflictStrategy.IGNORE)
 suspend fun seedProducts(values: List<ProductEntity>)

 @Upsert
 suspend fun saveProducts(values: List<ProductEntity>)

 @Query(
  """
        SELECT * FROM cart_items
        WHERE ownerAlias = :alias
        ORDER BY productId
        """
 )
 fun cartRaw(alias: String): Flow<List<CartItemEntity>>

 @Query(
  """
        SELECT * FROM cart_items
        WHERE ownerAlias = :alias AND productId = :product
        LIMIT 1
        """
 )
 suspend fun getCartItem(
  alias: String,
  product: String
 ): CartItemEntity?

 @Insert(onConflict = OnConflictStrategy.REPLACE)
 suspend fun upsertCart(value: CartItemEntity)

 @Query("DELETE FROM cart_items WHERE ownerAlias = :alias")
 suspend fun clearCart(alias: String)

 @Query(
  """
        DELETE FROM cart_items
        WHERE ownerAlias = :alias AND productId = :product
        """
 )
 suspend fun removeCart(
  alias: String,
  product: String
 )
}

// Los pedidos quedan guardados en el celular.
@Entity(
 tableName = "local_orders",
 indices = [
  Index(value = ["clientAlias"])
 ]
)
data class LocalOrderEntity(
 @PrimaryKey val id: String,
 val clientAlias: String,
 val createdAt: String,
 val payload: String
)

@Dao
interface LocalOrderDao {

 @Query("SELECT * FROM local_orders ORDER BY createdAt DESC")
 suspend fun all(): List<LocalOrderEntity>

 @Query(
  """
        SELECT * FROM local_orders
        WHERE clientAlias = :alias
        ORDER BY createdAt DESC
        """
 )
 suspend fun byOwner(alias: String): List<LocalOrderEntity>

 @Query("SELECT * FROM local_orders WHERE id = :id LIMIT 1")
 suspend fun byId(id: String): LocalOrderEntity?

 @Upsert
 suspend fun save(order: LocalOrderEntity)
}

@Database(
 entities = [
  ClientEntity::class,
  ProductEntity::class,
  CartItemEntity::class,
  LocalOrderEntity::class,
  UserAvatarEntity::class
 ],
 version = 3,
 exportSchema = false
)
 abstract class OylDatabase : RoomDatabase() {

 abstract fun clientDao(): ClientDao

 abstract fun catalogDao(): CatalogDao

 abstract fun orderDao(): LocalOrderDao

 abstract fun avatarDao(): AvatarDao
}

// Añade la tabla de pedidos sin borrar las tablas anteriores.
val OYL_MIGRATION_1_2 = object : Migration(1, 2) {

 override fun migrate(db: SupportSQLiteDatabase) {
  db.execSQL(
   """
            CREATE TABLE IF NOT EXISTS local_orders (
                id TEXT NOT NULL,
                clientAlias TEXT NOT NULL,
                createdAt TEXT NOT NULL,
                payload TEXT NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent()
  )

  db.execSQL(
   """
            CREATE INDEX IF NOT EXISTS index_local_orders_clientAlias
            ON local_orders(clientAlias)
            """.trimIndent()
  )
 }
}