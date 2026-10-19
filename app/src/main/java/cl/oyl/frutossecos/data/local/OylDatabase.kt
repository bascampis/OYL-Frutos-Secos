package cl.oyl.frutossecos.data.local

import androidx.room.*

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
@Database(entities = [ClientEntity::class], version = 1, exportSchema = false)
abstract class OylDatabase: RoomDatabase() { abstract fun clientDao(): ClientDao }