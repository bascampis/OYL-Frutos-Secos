package cl.oyl.frutossecos.data.local

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

/** Avatar elegido por cada usuario (1, 2 o 3) y, si tiene, la ruta de su foto. */
@Entity(tableName = "user_avatars")
data class UserAvatarEntity(
    @PrimaryKey val alias: String,
    val avatarId: Int,
    val photoPath: String?
)

@Dao
interface AvatarDao {

    @Query("SELECT * FROM user_avatars WHERE alias = :alias LIMIT 1")
    fun observe(alias: String): Flow<UserAvatarEntity?>

    @Upsert
    suspend fun save(value: UserAvatarEntity)
}

// Agrega la tabla de avatares sin borrar nada de lo anterior.
val OYL_MIGRATION_2_3 = object : Migration(2, 3) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS user_avatars (
                alias TEXT NOT NULL,
                avatarId INTEGER NOT NULL,
                photoPath TEXT,
                PRIMARY KEY(alias)
            )
            """.trimIndent()
        )
    }
}