package cl.oyl.frutossecos.data

import cl.oyl.frutossecos.data.model.LocalOrder
import cl.oyl.frutossecos.data.model.LocalOrderItem
import cl.oyl.frutossecos.data.model.LocalOrderRequest
import cl.oyl.frutossecos.data.model.LocalProduct
import cl.oyl.frutossecos.data.model.ProductInputDto
import cl.oyl.frutossecos.data.model.StockDeltaDto
import cl.oyl.frutossecos.data.remote.CatalogApi
import cl.oyl.frutossecos.data.remote.LoginBody
import cl.oyl.frutossecos.data.remote.OrderApi
import cl.oyl.frutossecos.data.remote.RegisterBody
import cl.oyl.frutossecos.data.remote.RemoteRepository
import cl.oyl.frutossecos.data.remote.RemoteUser
import cl.oyl.frutossecos.data.remote.UserApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class RemoteRepositoryTest {

    private val catalog = mockk<CatalogApi>()
    private val orders = mockk<OrderApi>()
    private val users = mockk<UserApi>()
    private val repo = RemoteRepository(catalog, orders, users)

    private fun product(id: String = "OY001", stock: Int = 10) =
        LocalProduct(id, "Almendras", "Frutos secos", "desc", "250 g", 2990, stock, true, null)

    private fun input(id: String = "OY001") =
        ProductInputDto(id, "Almendras", "Frutos secos", "desc", "250 g", 2990, 10, true, null)

    private fun httpError(code: Int, json: String) = HttpException(
        Response.error<Any>(code, json.toResponseBody("application/json".toMediaType()))
    )

    private fun user(role: String = "CLIENT") = RemoteUser(1, "Ana", "ana@correo.cl", "+56912345678", "", role)

    // ---------- Catálogo ----------
    @Test fun products_returnsListFromServer() = runTest {
        coEvery { catalog.products() } returns listOf(product("OY001"), product("OY002"))
        val r = repo.products()
        assertTrue(r.isSuccess)
        assertEquals(2, r.getOrThrow().size)
    }

    @Test fun createProduct_returnsCreated() = runTest {
        coEvery { catalog.create(any()) } returns product("OY100")
        assertEquals("OY100", repo.createProduct(input("OY100")).getOrThrow().id)
    }

    @Test fun updateProduct_usesProductIdInPath() = runTest {
        coEvery { catalog.update("OY001", any()) } returns product()
        repo.updateProduct(input("OY001"))
        coVerify { catalog.update("OY001", any()) }
    }

    @Test fun deleteProduct_successWhenServerReturns204() = runTest {
        coEvery { catalog.delete("OY001") } returns Response.success(Unit)
        assertTrue(repo.deleteProduct("OY001").isSuccess)
    }

    @Test fun deleteProduct_failsWhenServerReturns404() = runTest {
        coEvery { catalog.delete("X") } returns
            Response.error(404, """{"message":"Producto no encontrado"}""".toResponseBody("application/json".toMediaType()))
        assertTrue(repo.deleteProduct("X").isFailure)
    }

    @Test fun adjustStock_sendsDelta() = runTest {
        coEvery { catalog.adjustStock("OY001", StockDeltaDto(-3)) } returns product(stock = 7)
        assertEquals(7, repo.adjustStock("OY001", -3).getOrThrow().stock)
    }

    // ---------- Manejo de errores ----------
    @Test fun httpError_usesServerMessage() = runTest {
        coEvery { catalog.create(any()) } throws httpError(409, """{"message":"Ya existe un producto con ese código"}""")
        val r = repo.createProduct(input())
        assertEquals("Ya existe un producto con ese código", r.exceptionOrNull()?.message)
    }

    @Test fun httpError_withoutBody_usesStatusCode() = runTest {
        coEvery { catalog.products() } throws httpError(500, "no es json")
        assertEquals("Error del servidor (500)", repo.products().exceptionOrNull()?.message)
    }

    @Test fun networkError_showsConnectionMessage() = runTest {
        coEvery { catalog.products() } throws IOException("timeout")
        assertTrue(repo.products().exceptionOrNull()?.message!!.contains("No se pudo conectar"))
    }

    @Test fun unexpectedError_keepsMessage() = runTest {
        coEvery { catalog.products() } throws IllegalStateException("boom")
        assertEquals("boom", repo.products().exceptionOrNull()?.message)
    }

    // ---------- Pedidos ----------
    private fun order(status: String = "Pendiente de Validación") =
        LocalOrder("o1", "cliente", "2026-11-18T10:00:00Z", 2990, status, "Retiro", emptyList())

    @Test fun placeOrder_returnsOrder() = runTest {
        val req = LocalOrderRequest("cliente", "Retiro", listOf(LocalOrderItem("OY001", 1)))
        coEvery { orders.create(req) } returns order()
        assertEquals("o1", repo.placeOrder(req).getOrThrow().id)
    }

    @Test fun placeOrder_stockConflictShowsMessage() = runTest {
        val req = LocalOrderRequest("cliente", "Retiro", listOf(LocalOrderItem("OY001", 99)))
        coEvery { orders.create(req) } throws httpError(409, """{"message":"Stock insuficiente: Almendras"}""")
        assertEquals("Stock insuficiente: Almendras", repo.placeOrder(req).exceptionOrNull()?.message)
    }

    @Test fun ordersOf_filtersByClient() = runTest {
        coEvery { orders.byClient("cliente") } returns listOf(order())
        assertEquals(1, repo.ordersOf("cliente").getOrThrow().size)
    }

    @Test fun allOrders_returnsEveryOrder() = runTest {
        coEvery { orders.all() } returns listOf(order(), order("Entregado"))
        assertEquals(2, repo.allOrders().getOrThrow().size)
    }

    @Test fun setOrderStatus_returnsUpdatedOrder() = runTest {
        coEvery { orders.setStatus("o1", any()) } returns order("Confirmado")
        assertEquals("Confirmado", repo.setOrderStatus("o1", "Confirmado").getOrThrow().status)
    }

    @Test fun cancelOrder_successAndFailure() = runTest {
        coEvery { orders.cancel("o1") } returns Response.success(Unit)
        coEvery { orders.cancel("o2") } returns
            Response.error(409, "{}".toResponseBody("application/json".toMediaType()))
        assertTrue(repo.cancelOrder("o1").isSuccess)
        assertTrue(repo.cancelOrder("o2").isFailure)
    }

    // ---------- Usuarios ----------
    @Test fun register_returnsUser() = runTest {
        val body = RegisterBody("Ana", "ana@correo.cl", "+56912345678", "", "Clave1234")
        coEvery { users.register(body) } returns user()
        assertEquals("CLIENT", repo.register(body).getOrThrow().role)
    }

    @Test fun login_returnsUserWhenCredentialsAreValid() = runTest {
        coEvery { users.login(LoginBody("admin@admin.cl", "DemoOYL2026!")) } returns user("ADMIN")
        assertEquals("ADMIN", repo.login("admin@admin.cl", "DemoOYL2026!").getOrThrow().role)
    }

    @Test fun login_invalidCredentialsFails() = runTest {
        coEvery { users.login(any()) } throws httpError(401, """{"message":"Credenciales inválidas"}""")
        assertEquals("Credenciales inválidas", repo.login("a@b.cl", "mala").exceptionOrNull()?.message)
    }

    @Test fun users_listAndDelete() = runTest {
        coEvery { users.users() } returns listOf(user())
        coEvery { users.delete(1) } returns Response.success(Unit)
        assertEquals(1, repo.users().getOrThrow().size)
        assertTrue(repo.deleteUser(1).isSuccess)
    }
}
