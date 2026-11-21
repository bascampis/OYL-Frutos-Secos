package cl.oyl.frutossecos.viewmodel

import android.app.Application
import cl.oyl.frutossecos.MainDispatcherRule
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.awaitTrue
import cl.oyl.frutossecos.data.StoreFixture
import cl.oyl.frutossecos.data.StoreFixture.Companion.entity
import cl.oyl.frutossecos.data.StoreFixture.Companion.order
import cl.oyl.frutossecos.data.StoreFixture.Companion.product
import cl.oyl.frutossecos.data.local.CartItemEntity
import cl.oyl.frutossecos.data.local.ClientDao
import cl.oyl.frutossecos.data.local.ClientEntity
import cl.oyl.frutossecos.data.remote.ApiException
import cl.oyl.frutossecos.data.remote.ServerUnavailableException
import cl.oyl.frutossecos.data.repository.CatalogRepository
import cl.oyl.frutossecos.data.repository.LocalStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class StoreViewModelsTest {

    @get:Rule val main = MainDispatcherRule()

    private lateinit var f: StoreFixture
    private val down = Result.failure<Nothing>(ServerUnavailableException("sin conexión"))

    @Before fun setUp() {
        f = StoreFixture()
        coEvery { f.remote.products() } returns down
        coEvery { f.catalog.productList() } returns listOf(entity("OY001", stock = 10))
        LocalStore.initialize(f.db, f.remote)
    }

    @After fun tearDown() { f.close() }

    // ================= ProductManagementViewModel =================
    private fun pm(): ProductManagementViewModel {
        // El servidor responde al cargar la lista; así no se activa la pausa por "servidor caído".
        coEvery { f.remote.products() } returns Result.success(listOf(product("OY001", stock = 10)))
        coEvery { f.remote.saveProduct(any()) } returns down
        coEvery { f.catalog.getProduct(any()) } returns entity("OY001", stock = 10)
        return ProductManagementViewModel()
    }

    @Test fun pm_loadsProductsOnInit() {
        val vm = pm()
        assertEquals(1, vm.products.size)
        assertFalse(vm.loading)
    }

    @Test fun pm_loadFailureShowsMessage() {
        coEvery { f.catalog.productList() } throws RuntimeException("db")
        val vm = ProductManagementViewModel()
        assertEquals("No se pudieron cargar los productos locales.", vm.message)
    }

    @Test fun pm_openCreateOpenEditAndClose() {
        val vm = pm()
        vm.openCreate()
        assertTrue(vm.dialogOpen); assertFalse(vm.editing)
        vm.closeDialog(); assertFalse(vm.dialogOpen)
        vm.openEdit(product("OY001"))
        assertTrue(vm.editing)
        assertEquals("Almendras", vm.form.name)
    }

    @Test fun pm_updateFieldRules() {
        val vm = pm()
        vm.openCreate()
        vm.updateField("id", "OY500"); vm.updateField("name", "Maní")
        vm.updateField("category", "Frutos secos"); vm.updateField("description", "desc")
        vm.updateField("presentation", "250 g"); vm.updateField("price", "1990")
        vm.updateField("stock", "5"); vm.updateField("imageUrl", " https://a.cl/x.png ")
        vm.updateField("otro", "x")
        assertEquals("OY500", vm.form.id)
        assertEquals("5", vm.form.stock)
        assertEquals("https://a.cl/x.png", vm.form.imageUrl)
        vm.openEdit(product("OY001"))
        vm.updateField("id", "CAMBIO"); vm.updateField("stock", "99")
        assertEquals("OY001", vm.form.id)
        assertEquals("10", vm.form.stock)
    }

    @Test fun pm_invalidSaveKeepsDialogAndRevalidatesOnEdit() {
        val vm = pm()
        vm.openCreate()
        vm.save()
        assertTrue(vm.errors.isNotEmpty())
        vm.updateField("name", "Maní")
        assertTrue(vm.errors.containsKey("id"))
    }

    private fun fillNew(vm: ProductManagementViewModel) {
        vm.openCreate()
        vm.updateField("id", "OY500"); vm.updateField("name", "Maní")
        vm.updateField("description", "Maní salado"); vm.updateField("presentation", "250 g")
        vm.updateField("price", "1990")
    }

    @Test fun pm_createProduct() {
        val vm = pm()
        coEvery { f.catalog.getProduct("OY500") } returns null
        fillNew(vm)
        vm.save()
        assertFalse(vm.dialogOpen)
        assertEquals("Producto creado en el celular.", vm.message)
        assertFalse(vm.saving)
    }

    @Test fun pm_editProduct() {
        val vm = pm()
        vm.openEdit(product("OY001"))
        vm.updateField("price", "3000")
        vm.save()
        assertEquals("Producto actualizado en el celular.", vm.message)
    }

    @Test fun pm_editMissingProductShowsFormError() {
        val vm = pm()
        vm.openEdit(product("ZZZ"))
        vm.save()
        assertEquals("No se pudo guardar el producto local.", vm.formError)
    }

    @Test fun pm_toggleActiveBothWays() {
        val vm = pm()
        vm.toggleActive(product("OY001"))
        assertEquals("Producto desactivado.", vm.message)
        coEvery { f.catalog.productList() } returns listOf(entity("OY001", active = false))
        vm.toggleActive(product("OY001", active = false))
        assertEquals("Producto activado.", vm.message)
    }

    @Test fun pm_toggleMissingProductFails() {
        val vm = pm()
        vm.toggleActive(product("NOEXISTE"))
        assertEquals("No se pudo cambiar el estado del producto.", vm.message)
    }

    @Test fun pm_deleteFlow() {
        val vm = pm()
        vm.confirmDelete()                       // sin selección: no hace nada
        vm.askDelete(product("OY001")); vm.dismissDelete()
        assertNull(vm.productToDelete)
        coEvery { f.remote.deleteProduct("OY001") } returns Result.success(Unit)
        vm.askDelete(product("OY001")); vm.confirmDelete()
        assertEquals("Producto eliminado.", vm.message)
        coVerify { f.catalog.deleteProduct("OY001") }
    }

    @Test fun pm_deleteShowsServerMessage() {
        val vm = pm()
        coEvery { f.remote.deleteProduct(any()) } returns Result.failure(ApiException(409, "Tiene pedidos"))
        vm.askDelete(product("OY001")); vm.confirmDelete()
        assertEquals("Tiene pedidos", vm.message)
    }

    // ================= OrderConfirmationViewModel =================
    private val app = mockk<Application>(relaxed = true)

    @Test fun confirmation_findsTheOrder() {
        coEvery { f.remote.ordersOf("cliente") } returns Result.success(listOf(order("o1")))
        val vm = OrderConfirmationViewModel(app)
        vm.load("cliente", "o1")
        assertEquals("o1", vm.state.value.order?.id)
        vm.load("cliente", "o1")                  // misma petición: no se repite
        coVerify(exactly = 1) { f.remote.ordersOf("cliente") }
    }

    @Test fun confirmation_orderNotFound() {
        coEvery { f.remote.ordersOf("cliente") } returns Result.success(listOf(order("o1")))
        val vm = OrderConfirmationViewModel(app)
        vm.load("cliente", "otro")
        assertEquals("No se encontró este pedido para tu cuenta.", vm.state.value.error)
    }

    @Test fun confirmation_localFailure() {
        coEvery { f.remote.ordersOf(any()) } returns down
        coEvery { f.orders.byOwner(any()) } throws RuntimeException("db")
        val vm = OrderConfirmationViewModel(app)
        vm.load("cliente", "o1")
        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.loading)
    }

    // ================= CartViewModel =================
    private val catalogRepo = mockk<CatalogRepository>()
    private val clientDao = mockk<ClientDao>()
    private val cartApp = mockk<OylApplication>(relaxed = true)

    private fun cart(items: List<CartItemEntity> = listOf(CartItemEntity("cliente", "OY001", 2))): CartViewModel {
        every { cartApp.catalogRepository } returns catalogRepo
        every { cartApp.database } returns f.db
        every { cartApp.applicationContext } returns cartApp
        every { f.db.clientDao() } returns clientDao
        coEvery { clientDao.byAlias("cliente") } returns ClientEntity("cliente", "Cata", "c@c.cl", "+56912345678", "dir", "s", "h")
        every { catalogRepo.cart("cliente") } returns flowOf(items)
        every { f.catalog.allProducts() } returns flowOf(listOf(entity("OY001", stock = 10)))
        coEvery { f.remote.products() } returns Result.success(listOf(product("OY001", stock = 10)))
        return CartViewModel(cartApp)
    }

    @Test fun cart_startLoadsRowsAndContact() {
        val vm = cart()
        vm.start("cliente")
        vm.start("cliente")                       // repetido: se ignora
        val s = vm.state.value
        assertEquals(1, s.rows.size)
        assertEquals(5980L, s.subtotal)
        assertEquals("Almendras", s.rows.single().name)
        assertEquals("Cata", s.contact?.name)
        assertFalse(s.loading)
    }

    @Test(expected = IllegalStateException::class)
    fun cart_startWithAnotherAliasFails() {
        val vm = cart()
        vm.start("cliente")
        vm.start("otro")
    }

    @Test fun cart_startFailureShowsMessage() {
        val vm = cart()
        every { catalogRepo.cart("cliente") } throws RuntimeException("db")
        vm.start("cliente")
        assertEquals("No se pudo cargar el carrito local.", vm.state.value.message)
    }

    @Test fun cart_changeCallsRepositoryAndHandlesErrors() {
        val vm = cart()
        vm.change("OY001", 1)                     // sin start: se ignora
        coVerify(exactly = 0) { catalogRepo.changeCart(any(), any(), any()) }
        vm.start("cliente")
        coEvery { catalogRepo.changeCart("cliente", "OY001", 1) } returns Unit
        vm.change("OY001", 1)
        coVerify { catalogRepo.changeCart("cliente", "OY001", 1) }
        assertFalse(vm.state.value.busy)
        coEvery { catalogRepo.changeCart("cliente", "OY001", -1) } throws RuntimeException("x")
        vm.change("OY001", -1)
        assertEquals("No se pudo actualizar el carrito.", vm.state.value.message)
    }

    @Test fun cart_reviewOk_thenCloseReview() {
        val vm = cart(); vm.start("cliente")
        vm.review()
        assertTrue(vm.state.value.reviewOpen)
        vm.closeReview()
        assertFalse(vm.state.value.reviewOpen)
    }

    @Test fun cart_reviewReportsStockProblems() {
        val vm = cart(); vm.start("cliente")
        coEvery { f.remote.products() } returns Result.success(listOf(product("OY001", stock = 1)))
        coEvery { f.catalog.productList() } returns listOf(entity("OY001", stock = 1))
        vm.review()
        assertFalse(vm.state.value.reviewOpen)
        assertTrue(vm.state.value.message!!.contains("quedan 1"))
    }

    @Test fun cart_reviewReportsPriceChange() {
        val vm = cart(); vm.start("cliente")
        coEvery { f.remote.products() } returns Result.success(listOf(product("OY001", price = 3500)))
        coEvery { f.catalog.productList() } returns listOf(entity("OY001", price = 3500))
        vm.review()
        assertEquals("Los precios cambiaron. Revisa el carrito antes de continuar.", vm.state.value.message)
    }

    @Test fun cart_reviewFailureShowsMessage() {
        val vm = cart(); vm.start("cliente")
        coEvery { f.catalog.productList() } throws RuntimeException("fallo")
        vm.review()
        assertEquals("fallo", vm.state.value.message)
    }

    @Test fun cart_confirmCompletesTheOrder() {
        val vm = cart(); vm.start("cliente")
        vm.confirm()                              // sin revisión previa: se ignora
        assertNull(vm.state.value.completedOrder)
        vm.review()
        coEvery { f.remote.placeOrder(any()) } returns Result.success(order("o9"))
        vm.confirm()
        awaitTrue { !vm.state.value.busy }
        assertEquals("o9", vm.state.value.completedOrder?.id)
        vm.change("OY001", 1)                     // con pedido terminado no cambia nada
        coVerify(exactly = 0) { catalogRepo.changeCart(any(), any(), any()) }
    }

    @Test fun cart_confirmShowsServerRejection() {
        val vm = cart(); vm.start("cliente")
        vm.review()
        coEvery { f.remote.placeOrder(any()) } returns Result.failure(ApiException(409, "Stock insuficiente: Almendras"))
        vm.confirm()
        assertEquals("Stock insuficiente: Almendras", vm.state.value.message)
        assertFalse(vm.state.value.reviewOpen)
    }
}
