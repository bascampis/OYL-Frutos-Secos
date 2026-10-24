package cl.oyl.frutossecos.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "map_points")
data class MapPointEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val address: String,
    val hours: String,
    val latitude: Double,
    val longitude: Double,
    val instructions: String
)

@Dao
interface MapPointDao {
    @Query("SELECT * FROM map_points ORDER BY type, name")
    fun observeAll(): Flow<List<MapPointEntity>>

    @Query("SELECT COUNT(*) FROM map_points")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(points: List<MapPointEntity>)
}

// Base independiente: no cambia la versión ni los datos de clientes/carritos.
@Database(entities = [MapPointEntity::class], version = 1, exportSchema = false)
abstract class MapPointsDatabase : RoomDatabase() {
    abstract fun points(): MapPointDao

    companion object {
        @Volatile private var instance: MapPointsDatabase? = null

        fun get(context: Context): MapPointsDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                MapPointsDatabase::class.java,
                "oyl_map_points.db"
            ).build().also { instance = it }
        }
    }
}

class MapPointsRepository(private val database: MapPointsDatabase) {
    val points = database.points().observeAll()

    suspend fun seedDemoPoints() = database.withTransaction {
        if (database.points().count() == 0) {
            database.points().insertAll(listOf(
                MapPointEntity("DEMO_CENTRO", "Tienda OYL Centro (demo)", "Tienda",
                    "Referencia de prueba: sector Universidad de Chile, Santiago",
                    "Lun–Vie 09:00–19:00 (horario ficticio)", -33.4444, -70.6504,
                    "Punto académico de prueba. Aquí no opera una tienda OYL confirmada."),
                MapPointEntity("DEMO_NORTE", "Tienda OYL Norte (demo)", "Tienda",
                    "Referencia de prueba: sector Cal y Canto, Santiago",
                    "Lun–Sáb 10:00–18:00 (horario ficticio)", -33.4325, -70.6535,
                    "Punto académico de prueba. Reemplazar por una dirección autorizada."),
                MapPointEntity("DEMO_ORIENTE", "Retiro Oriente (demo)", "Retiro",
                    "Referencia de prueba: sector Baquedano, Santiago",
                    "Lun–Vie 10:00–18:00 (horario ficticio)", -33.4372, -70.6344,
                    "Punto de retiro ficticio; no acudir para retirar pedidos reales."),
                MapPointEntity("DEMO_SUR", "Retiro Sur (demo)", "Retiro",
                    "Referencia de prueba: sector Parque Almagro, Santiago",
                    "Lun–Sáb 10:00–17:00 (horario ficticio)", -33.4524, -70.6507,
                    "Punto de retiro ficticio utilizado para probar filtros y marcadores.")
            ))
        }
    }
}
