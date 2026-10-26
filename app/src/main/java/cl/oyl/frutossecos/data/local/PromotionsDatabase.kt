package cl.oyl.frutossecos.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "promotions")
data class PromotionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val productId: String,
    val startDate: String,
    val endDate: String,
    val active: Boolean = true
)

@Dao
interface PromotionDao {

    @Query("SELECT * FROM promotions ORDER BY startDate DESC, title")
    fun observe(): Flow<List<PromotionEntity>>

    @Upsert
    suspend fun save(value: PromotionEntity)
}

@Database(
    entities = [PromotionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PromotionsDatabase : RoomDatabase() {

    abstract fun promotionDao(): PromotionDao

    companion object {
        @Volatile
        private var instance: PromotionsDatabase? = null

        fun get(context: Context): PromotionsDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PromotionsDatabase::class.java,
                    "oyl_promotions.db"
                ).build().also {
                    instance = it
                }
            }
        }
    }
}