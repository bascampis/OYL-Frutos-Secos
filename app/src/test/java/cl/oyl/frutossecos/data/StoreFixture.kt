package cl.oyl.frutossecos.data

import androidx.room.withTransaction
import cl.oyl.frutossecos.data.local.CatalogDao
import cl.oyl.frutossecos.data.local.LocalOrderDao
import cl.oyl.frutossecos.data.local.OylDatabase
import cl.oyl.frutossecos.data.local.ProductEntity
import cl.oyl.frutossecos.data.model.LocalOrder
import cl.oyl.frutossecos.data.model.LocalOrderLine
import cl.oyl.frutossecos.data.model.LocalProduct
import cl.oyl.frutossecos.data.remote.RemoteRepository
import cl.oyl.frutossecos.data.repository.LocalStoreRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.fail

/** Base de datos Room falsa (DAOs simulados) para probar los repositorios sin Android. */
class StoreFixture {
    val catalog = mockk<CatalogDao>(relaxed = true)
    val orders = mockk<LocalOrderDao>(relaxed = true)
    val remote = mockk<RemoteRepository>()
    val db = mockk<OylDatabase>(relaxed = true)

    private val roomClasses = listOf("androidx.room.RoomDatabaseKt", "androidx.room.RoomDatabaseExtKt")
        .filter { name -> runCatching { Class.forName(name) }.isSuccess }

    init {
        every { db.catalogDao() } returns catalog
        every { db.orderDao() } returns orders
        roomClasses.forEach { mockkStatic(it) }
        coEvery { db.withTransaction(any<suspend () -> Any>()) } coAnswers {
            secondArg<suspend () -> Any>().invoke()
        }
    }

    val repo = LocalStoreRepository(db, remote)

    fun close() {
        roomClasses.forEach { unmockkStatic(it) }
    }

    companion object {
        fun entity(id: String = "OY001", stock: Int = 10, price: Int = 2990, active: Boolean = true) =
            ProductEntity(id, "Almendras", "Frutos secos", "desc", "250 g", price, stock, active, null)

        fun product(id: String = "OY001", stock: Int = 10, price: Int = 2990, active: Boolean = true) =
            LocalProduct(id, "Almendras", "Frutos secos", "desc", "250 g", price, stock, active, null)

        fun order(id: String = "o1", status: String = "Pendiente de Validación", alias: String = "cliente") =
            LocalOrder(
                id, alias, "2026-11-20T10:00:00Z", 5980, status, "Retiro",
                listOf(LocalOrderLine(0, "OY001", "Almendras", 2, 2990))
            )
    }
}

/** Comprueba que el bloque falla con IllegalArgumentException (y opcionalmente con ese mensaje). */
suspend fun expectRejection(message: String? = null, block: suspend () -> Unit) {
    try {
        block()
    } catch (e: IllegalArgumentException) {
        if (message != null) org.junit.Assert.assertEquals(message, e.message)
        return
    }
    fail("Se esperaba un IllegalArgumentException")
}
