package cl.oyl.frutossecos.data

import cl.oyl.frutossecos.data.StoreFixture.Companion.entity
import cl.oyl.frutossecos.data.StoreFixture.Companion.order
import cl.oyl.frutossecos.data.StoreFixture.Companion.product
import cl.oyl.frutossecos.data.local.LocalOrderEntity
import cl.oyl.frutossecos.data.model.LocalOrderItem
import cl.oyl.frutossecos.data.model.LocalOrderRequest
import cl.oyl.frutossecos.data.model.OrderStatusDto
import cl.oyl.frutossecos.data.model.ProductInputDto
import cl.oyl.frutossecos.data.model.StockDeltaDto
import cl.oyl.frutossecos.data.remote.ApiException
import cl.oyl.frutossecos.data.remote.ServerUnavailableException
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocalStoreRepositoryTest {

    private lateinit var f: StoreFixture

    @Before fun setUp() { f = StoreFixture() }
    @After fun tearDown() { f.close() }

    private val down = Result.failure<Nothing>(ServerUnavailableException("sin conexión"))

    private fun input(price: Int = 2990, stock: Int = 5, image: String? = null, id: String = "OY100") =
        ProductInputDto(id, "Maní", "Frutos secos", "desc", "250 g", price, stock, true, image)

    private fun entityOf(o: cl.oyl.frutossecos.data.model.LocalOrder) =
        LocalOrderEntity(o.id, o.clientAlias, o.createdAt, Gson().toJson(o))

    // ---------- products ----------
    @Test fun products_syncsFromServerAndReturnsLocalList() = runTest {
        coEvery { f.remote.products() } returns Result.success(listOf(product(stock = 7)))
        coEvery { f.catalog.productCount() } returns 6
        coEvery { f.catalog.productList() } returns listOf(entity(stock = 7))
        val list = f.repo.products()
        assertEquals(7, list.single().stock)
        coVerify { f.catalog.saveProducts(any()) }
    }

    @Test fun products_usesLocalCopyWhenServerIsDown() = runTest {
        coEvery { f.remote.products() } returns down
        coEvery { f.catalog.productCount() } returns 6
        coEvery { f.catalog.productList() } returns listOf(entity())
        assertEquals(1, f.repo.products().size)
        coVerify(exactly = 0) { f.catalog.saveProducts(any()) }
    }

    // ---------- saveProduct ----------
    @Test fun saveProduct_rejectsInvalidData() = runTest {
        expectRejection("Identificador y nombre obligatorios.") { f.repo.saveProduct(input().copy(id = "")) }
        expectRejection("Completa categoría, descripción y presentación.") { f.repo.saveProduct(input().copy(description = "")) }
        expectRejection("El precio debe ser mayor que cero.") { f.repo.saveProduct(input(price = 0)) }
        expectRejection("El stock no puede ser negativo.") { f.repo.saveProduct(input(stock = -1)) }
        expectRejection("La URL de la imagen debe comenzar con https://.") { f.repo.saveProduct(input(image = "http://x.cl/a.png")) }
    }

    @Test fun saveProduct_usesTheStockReturnedByTheServer() = runTest {
        coEvery { f.remote.saveProduct(any()) } returns Result.success(product("OY100", stock = 99))
        coEvery { f.catalog.getProduct("OY100") } returns null
        assertEquals(99, f.repo.saveProduct(input()).stock)
        coVerify { f.catalog.saveProducts(any()) }
    }

    @Test fun saveProduct_showsTheServerMessageWhenRejected() = runTest {
        coEvery { f.remote.saveProduct(any()) } returns Result.failure(ApiException(409, "Ya existe"))
        expectRejection("Ya existe") { f.repo.saveProduct(input()) }
    }

    @Test fun saveProduct_savesLocallyKeepingStockWhenServerIsDown() = runTest {
        coEvery { f.remote.saveProduct(any()) } returns down
        coEvery { f.catalog.getProduct("OY100") } returns entity("OY100", stock = 5)
        assertEquals(5, f.repo.saveProduct(input(stock = 50, image = " https://picsum.photos/200 ")).stock)
    }

    // ---------- adjustStock ----------
    @Test fun adjustStock_usesTheServerStock() = runTest {
        coEvery { f.remote.adjustStock("OY001", 2) } returns Result.success(product(stock = 12))
        coEvery { f.catalog.getProduct("OY001") } returns entity(stock = 10)
        assertEquals(12, f.repo.adjustStock("OY001", StockDeltaDto(2)).stock)
    }

    @Test fun adjustStock_computesLocallyWhenServerIsDown() = runTest {
        coEvery { f.remote.adjustStock(any(), any()) } returns down
        coEvery { f.catalog.getProduct("OY001") } returns entity(stock = 10)
        assertEquals(15, f.repo.adjustStock("OY001", StockDeltaDto(5)).stock)
    }

    @Test fun adjustStock_rejectsNegativeStock() = runTest {
        coEvery { f.remote.adjustStock(any(), any()) } returns down
        coEvery { f.catalog.getProduct("OY001") } returns entity(stock = 3)
        expectRejection("El movimiento produce un stock inválido.") { f.repo.adjustStock("OY001", StockDeltaDto(-5)) }
    }

    @Test fun adjustStock_showsServerRejection() = runTest {
        coEvery { f.remote.adjustStock(any(), any()) } returns Result.failure(ApiException(409, "Stock insuficiente o inválido"))
        expectRejection("Stock insuficiente o inválido") { f.repo.adjustStock("OY001", StockDeltaDto(-50)) }
    }

    @Test(expected = IllegalStateException::class)
    fun adjustStock_failsWhenTheProductDoesNotExist() = runTest {
        coEvery { f.remote.adjustStock(any(), any()) } returns down
        coEvery { f.catalog.getProduct("X") } returns null
        f.repo.adjustStock("X", StockDeltaDto(1))
        Unit
    }

    // ---------- order ----------
    private fun request(qty: Int = 2) = LocalOrderRequest("cliente", "Retiro", listOf(LocalOrderItem("OY001", qty)))

    @Test fun order_rejectsBlankAliasAndEmptyCart() = runTest {
        expectRejection("Inicia sesión para comprar.") { f.repo.order(LocalOrderRequest(" ", "Retiro", listOf(LocalOrderItem("OY001", 1)))) }
        expectRejection("El carrito está vacío.") { f.repo.order(LocalOrderRequest("cliente", "Retiro", emptyList())) }
    }

    @Test fun order_savesTheOrderCreatedByTheServer() = runTest {
        coEvery { f.remote.placeOrder(any()) } returns Result.success(order())
        coEvery { f.catalog.getProduct("OY001") } returns entity(stock = 10)
        val result = f.repo.order(request())
        assertEquals("o1", result.id)
        coVerify { f.orders.save(any()) }
        coVerify { f.catalog.clearCart("cliente") }
        coVerify { f.catalog.saveProducts(match { it.single().stock == 8 }) }
    }

    @Test fun order_showsTheServerMessageWhenStockIsNotEnough() = runTest {
        coEvery { f.remote.placeOrder(any()) } returns Result.failure(ApiException(409, "Stock insuficiente: Almendras"))
        expectRejection("Stock insuficiente: Almendras") { f.repo.order(request()) }
        coVerify(exactly = 0) { f.orders.save(any()) }
    }

    @Test fun order_isSavedLocallyWhenServerIsDown() = runTest {
        coEvery { f.remote.placeOrder(any()) } returns down
        coEvery { f.catalog.getProduct("OY001") } returns entity(stock = 10, price = 1000)
        val result = f.repo.order(request(3))
        assertEquals(3000, result.total)
        assertEquals("Pendiente de Validación", result.status)
        coVerify { f.orders.save(any()) }
        coVerify { f.catalog.clearCart("cliente") }
    }

    @Test fun order_localValidationsWhenServerIsDown() = runTest {
        coEvery { f.remote.placeOrder(any()) } returns down
        coEvery { f.catalog.getProduct("OY001") } returns entity(stock = 1)
        expectRejection("Stock insuficiente para Almendras.") { f.repo.order(request(5)) }
        coEvery { f.catalog.getProduct("OY001") } returns entity(stock = 10, active = false)
        expectRejection("Almendras ya no está disponible.") { f.repo.order(request(1)) }
        coEvery { f.catalog.getProduct("OY001") } returns entity(stock = 10, price = 0)
        expectRejection("Precio inválido para Almendras.") { f.repo.order(request(1)) }
        expectRejection("La cantidad debe ser mayor que cero.") { f.repo.order(request(0)) }
    }

    @Test(expected = IllegalStateException::class)
    fun order_localFailsWhenProductDoesNotExist() = runTest {
        coEvery { f.remote.placeOrder(any()) } returns down
        coEvery { f.catalog.getProduct("OY001") } returns null
        f.repo.order(request())
        Unit
    }

    // ---------- orders / allOrders ----------
    @Test fun orders_returnsTheServerListAndCachesIt() = runTest {
        coEvery { f.remote.ordersOf("cliente") } returns Result.success(listOf(order("a"), order("b")))
        assertEquals(2, f.repo.orders("cliente").size)
        coVerify(exactly = 2) { f.orders.save(any()) }
    }

    @Test fun orders_usesRoomWhenServerIsDown() = runTest {
        coEvery { f.remote.ordersOf("cliente") } returns down
        coEvery { f.orders.byOwner("cliente") } returns listOf(entityOf(order("local")))
        assertEquals("local", f.repo.orders("cliente").single().id)
    }

    @Test fun allOrders_serverAndLocal() = runTest {
        coEvery { f.remote.allOrders() } returns Result.success(listOf(order("a")))
        assertEquals("a", f.repo.allOrders().single().id)
    }

    @Test fun allOrders_usesRoomWhenServerIsDown() = runTest {
        coEvery { f.remote.allOrders() } returns down
        coEvery { f.orders.all() } returns listOf(entityOf(order("x")), entityOf(order("y")))
        assertEquals(2, f.repo.allOrders().size)
    }

    // ---------- setStatus ----------
    @Test fun setStatus_usesTheServerResponse() = runTest {
        coEvery { f.remote.setOrderStatus("o1", "Confirmado") } returns Result.success(order(status = "Confirmado"))
        assertEquals("Confirmado", f.repo.setStatus("o1", OrderStatusDto("Confirmado")).status)
        coVerify { f.orders.save(any()) }
    }

    @Test fun setStatus_showsServerRejection() = runTest {
        coEvery { f.remote.setOrderStatus(any(), any()) } returns Result.failure(ApiException(400, "Estado no permitido"))
        expectRejection("Estado no permitido") { f.repo.setStatus("o1", OrderStatusDto("X")) }
    }

    @Test fun setStatus_advancesLocallyWhenServerIsDown() = runTest {
        coEvery { f.remote.setOrderStatus(any(), any()) } returns down
        coEvery { f.orders.byId("o1") } returns entityOf(order())
        assertEquals("Confirmado", f.repo.setStatus("o1", OrderStatusDto("Confirmado")).status)
    }

    @Test fun setStatus_rejectsSkippingStatesLocally() = runTest {
        coEvery { f.remote.setOrderStatus(any(), any()) } returns down
        coEvery { f.orders.byId("o1") } returns entityOf(order())
        expectRejection("Cambio de estado no permitido.") { f.repo.setStatus("o1", OrderStatusDto("Entregado")) }
    }

    @Test(expected = IllegalStateException::class)
    fun setStatus_failsWhenOrderDoesNotExist() = runTest {
        coEvery { f.remote.setOrderStatus(any(), any()) } returns down
        coEvery { f.orders.byId("zzz") } returns null
        f.repo.setStatus("zzz", OrderStatusDto("Confirmado"))
        Unit
    }

    // ---------- deleteProduct / cancelOrder ----------
    @Test fun deleteProduct_removesItFromServerAndRoom() = runTest {
        coEvery { f.remote.deleteProduct("OY001") } returns Result.success(Unit)
        f.repo.deleteProduct("OY001")
        coVerify { f.catalog.deleteProduct("OY001") }
    }

    @Test fun deleteProduct_removesLocalCopyWhenServerReturns404() = runTest {
        coEvery { f.remote.deleteProduct("OY001") } returns Result.failure(ApiException(404, "no existe"))
        f.repo.deleteProduct("OY001")
        coVerify { f.catalog.deleteProduct("OY001") }
    }

    @Test fun deleteProduct_failsWithoutConnection() = runTest {
        coEvery { f.remote.deleteProduct(any()) } returns down
        expectRejection("sin conexión") { f.repo.deleteProduct("OY001") }
        coVerify(exactly = 0) { f.catalog.deleteProduct(any()) }
    }

    @Test fun cancelOrder_restoresStockAndRemovesTheOrder() = runTest {
        coEvery { f.orders.byId("o1") } returns entityOf(order())
        coEvery { f.remote.cancelOrder("o1") } returns Result.success(Unit)
        coEvery { f.catalog.getProduct("OY001") } returns entity(stock = 5)
        f.repo.cancelOrder("o1")
        coVerify { f.catalog.saveProducts(match { it.single().stock == 7 }) }
        coVerify { f.orders.delete("o1") }
    }

    @Test fun cancelOrder_onlyAllowsPendingOrders() = runTest {
        coEvery { f.orders.byId("o1") } returns entityOf(order(status = "Entregado"))
        expectRejection("Solo se puede cancelar un pedido pendiente.") { f.repo.cancelOrder("o1") }
    }

    @Test fun cancelOrder_showsServerRejection() = runTest {
        coEvery { f.orders.byId("o1") } returns entityOf(order())
        coEvery { f.remote.cancelOrder("o1") } returns Result.failure(ApiException(409, "No se puede"))
        expectRejection("No se puede") { f.repo.cancelOrder("o1") }
    }

    @Test(expected = IllegalStateException::class)
    fun cancelOrder_failsWhenOrderDoesNotExist() = runTest {
        coEvery { f.orders.byId("nada") } returns null
        f.repo.cancelOrder("nada")
        Unit
    }

    @Test fun cancelOrder_workOfflineToo() = runTest {
        coEvery { f.orders.byId("o1") } returns entityOf(order())
        coEvery { f.remote.cancelOrder(any()) } returns down
        coEvery { f.catalog.getProduct("OY001") } returns null
        f.repo.cancelOrder("o1")
        assertTrue(true)
        coVerify { f.orders.delete("o1") }
    }
}
