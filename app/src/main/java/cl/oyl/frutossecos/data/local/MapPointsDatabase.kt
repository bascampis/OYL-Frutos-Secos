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

    // Actualiza los puntos existentes o inserta los que falten.
    @Upsert
    suspend fun saveAll(points: List<MapPointEntity>)
}

@Database(
    entities = [MapPointEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MapPointsDatabase : RoomDatabase() {

    abstract fun points(): MapPointDao

    companion object {
        @Volatile
        private var instance: MapPointsDatabase? = null

        fun get(context: Context): MapPointsDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MapPointsDatabase::class.java,
                    "oyl_map_points.db"
                ).build().also {
                    instance = it
                }
            }
        }
    }
}

class MapPointsRepository(
    private val database: MapPointsDatabase
) {
    val points: Flow<List<MapPointEntity>> =
        database.points().observeAll()

    suspend fun seedDemoPoints() {
        database.withTransaction {
            database.points().saveAll(
                listOf(
                    MapPointEntity(
                        id = "DEMO_CENTRO",
                        name = "OYL Alameda — Tienda Centro ",
                        type = "Tienda",
                        address = "Sector Metro Universidad de Chile, Santiago Centro.",
                        hours = "Lunes a viernes: 09:00 a 19:00. Sábado: 10:00 a 14:00.",
                        latitude = -33.4444,
                        longitude = -70.6504,
                        instructions = """
                            Punto de demostración del sector Universidad de Chile.

                            Atención simulada: venta de frutos secos, semillas y deshidratados.

                            Para probar Cómo llegar, utiliza el marcador de esta tienda.

                            Ubicación de referencia y horario ficticio. No existe una tienda OYL confirmada en este punto.
                        """.trimIndent()
                    ),

                    MapPointEntity(
                        id = "DEMO_NORTE",
                        name = "OYL Cal y Canto — Tienda Norte ",
                        type = "Tienda",
                        address = "Sector Metro Puente Cal y Canto, Santiago.",
                        hours = "Lunes a sábado: 10:00 a 18:00. Domingo: cerrado.",
                        latitude = -33.4325,
                        longitude = -70.6535,
                        instructions = """
                            Punto de demostración del sector Cal y Canto.

                            Atención simulada: consulta de productos y compras presenciales.

                            Selecciona este marcador para consultar su ubicación independiente de Tienda Centro.

                            Ubicación de referencia y horario ficticio. No existe una tienda OYL confirmada en este punto.
                        """.trimIndent()
                    ),

                    MapPointEntity(
                        id = "DEMO_ORIENTE",
                        name = "OYL Baquedano — Retiro Oriente ",
                        type = "Retiro",
                        address = "Sector Metro Baquedano, Santiago.",
                        hours = "Lunes a viernes: 11:00 a 18:00. Fin de semana: cerrado.",
                        latitude = -33.4372,
                        longitude = -70.6344,
                        instructions = """
                            Punto de retiro de demostración del sector Baquedano.

                            Retiro simulado de pedidos previamente confirmados.

                            Para la demostración, presenta el número de pedido que aparece en Mis pedidos.

                            Este punto es ficticio: no acudir para retirar compras reales.
                        """.trimIndent()
                    ),

                    MapPointEntity(
                        id = "DEMO_SUR",
                        name = "OYL Parque Almagro — Retiro Sur ",
                        type = "Retiro",
                        address = "Sector Parque Almagro, Santiago Centro.",
                        hours = "Lunes a sábado: 10:00 a 17:00. Domingo: cerrado.",
                        latitude = -33.4524,
                        longitude = -70.6507,
                        instructions = """
                            Punto de retiro de demostración del sector Parque Almagro.

                            Retiro simulado de pedidos preparados.

                            Comprueba el estado de la solicitud en Mis pedidos antes del retiro de demostración.

                            Este punto es ficticio: no acudir para retirar compras reales.
                        """.trimIndent()
                    )
                )
            )
        }
    }
}