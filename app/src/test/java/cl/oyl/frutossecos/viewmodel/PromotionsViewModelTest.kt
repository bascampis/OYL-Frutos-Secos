package cl.oyl.frutossecos.viewmodel

import android.content.SharedPreferences
import cl.oyl.frutossecos.MainDispatcherRule
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.data.StoreFixture
import cl.oyl.frutossecos.data.StoreFixture.Companion.entity
import cl.oyl.frutossecos.data.local.PromotionDao
import cl.oyl.frutossecos.data.local.PromotionEntity
import cl.oyl.frutossecos.data.local.PromotionsDatabase
import cl.oyl.frutossecos.data.repository.CatalogRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PromotionsViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private lateinit var f: StoreFixture
    private val dao = mockk<PromotionDao>(relaxed = true)
    private val app = mockk<OylApplication>(relaxed = true)
    private val catalogRepo = mockk<CatalogRepository>(relaxed = true)
    private val promo = PromotionEntity("p1", "2x1", "Descuento", "OY001", "2026-11-01", "2026-11-30", true)

    @Before fun setUp() {
        f = StoreFixture()
        val pdb = mockk<PromotionsDatabase>()
        every { pdb.promotionDao() } returns dao
        mockkObject(PromotionsDatabase.Companion)
        every { PromotionsDatabase.get(any()) } returns pdb
        every { dao.observe() } returns flowOf(listOf(promo))
        every { app.catalogRepository } returns catalogRepo
        every { app.database } returns f.db
        every { f.catalog.allProducts() } returns flowOf(listOf(entity("OY001")))
        coEvery { f.catalog.productList() } returns listOf(entity("OY001"))
        setRole("ADMINISTRADOR")
    }

    @After fun tearDown() { unmockkObject(PromotionsDatabase.Companion); f.close() }

    private fun setRole(role: String) {
        val prefs = mockk<SharedPreferences>()
        every { prefs.getString("role", "") } returns role
        every { app.getSharedPreferences("oyl_session", 0) } returns prefs
    }

    private fun valid() = PromotionForm("", "Oferta", "Descripción", "OY001", "2026-11-01", "2026-11-30", true)

    // ---- validador ----
    @Test fun validator_acceptsValidForm() {
        assertTrue(PromotionValidator.validate(valid(), listOf(entity("OY001"))).isEmpty())
    }

    @Test fun validator_reportsEveryProblem() {
        val e = PromotionValidator.validate(
            PromotionForm("", " ", " ", "NADA", "hoy", "mañana", true), listOf(entity("OY001"))
        )
        assertEquals(setOf("title", "description", "productId", "startDate", "endDate"), e.keys)
    }

    @Test fun validator_inactiveProductAndDateOrder() {
        val e = PromotionValidator.validate(
            valid().copy(startDate = "2026-12-01", endDate = "2026-11-01"), listOf(entity("OY001", active = false))
        )
        assertEquals("Selecciona un producto activo para publicar.", e["productId"])
        assertEquals("La fecha final no puede ser anterior al inicio.", e["endDate"])
    }

    // ---- ViewModel ----
    @Test fun loadsPromotionsAndProducts() {
        val vm = PromotionsViewModel(app)
        assertEquals(1, vm.promotions.size)
        assertEquals(1, vm.products.size)
        assertFalse(vm.loading)
    }

    @Test fun loadFailureShowsMessage() {
        coEvery { catalogRepo.seedDemoProducts() } throws RuntimeException("db")
        val vm = PromotionsViewModel(app)
        assertEquals("No se pudieron cargar las promociones.", vm.message)
        assertFalse(vm.loading)
    }

    @Test fun onlyAdminsCanOpenDialogs() {
        setRole("CLIENTE")
        val vm = PromotionsViewModel(app)
        vm.openCreate(); vm.openEdit(promo); vm.save()
        assertFalse(vm.dialogOpen)
    }

    @Test fun adminOpensCreateEditAndCloses() {
        val vm = PromotionsViewModel(app)
        vm.openCreate(); assertTrue(vm.dialogOpen)
        vm.openEdit(promo); assertEquals("2x1", vm.form.title)
        vm.closeDialog(); assertFalse(vm.dialogOpen)
    }

    @Test fun invalidSaveShowsErrorsThenRevalidatesOnUpdate() {
        val vm = PromotionsViewModel(app)
        vm.openCreate(); vm.save()
        assertTrue(vm.errors.isNotEmpty())
        vm.update(valid())
        assertTrue(vm.errors.isEmpty())
    }

    @Test fun savesValidPromotion() {
        val vm = PromotionsViewModel(app)
        vm.openCreate(); vm.update(valid()); vm.save()
        assertEquals("Promoción guardada.", vm.message)
        assertFalse(vm.dialogOpen); assertFalse(vm.saving)
        coVerify { dao.save(match { it.title == "Oferta" && it.id.isNotBlank() }) }
    }

    @Test fun saveFailureShowsFormError() {
        coEvery { dao.save(any()) } throws RuntimeException("db")
        val vm = PromotionsViewModel(app)
        vm.openCreate(); vm.update(valid()); vm.save()
        assertEquals("No se pudo guardar la promoción.", vm.formError)
    }
}
