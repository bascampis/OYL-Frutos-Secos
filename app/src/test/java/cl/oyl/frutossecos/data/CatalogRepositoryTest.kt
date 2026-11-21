package cl.oyl.frutossecos.data

import cl.oyl.frutossecos.data.local.CartItemEntity
import cl.oyl.frutossecos.data.local.CatalogDao
import cl.oyl.frutossecos.data.remote.ApiException
import cl.oyl.frutossecos.data.remote.RemoteRepository
import cl.oyl.frutossecos.data.remote.ServerUnavailableException
import cl.oyl.frutossecos.data.repository.CatalogRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CatalogRepositoryTest {

    private val dao = mockk<CatalogDao>(relaxed = true)
    private val remote = mockk<RemoteRepository>()
    private lateinit var repo: CatalogRepository
    private val down = Result.failure<Nothing>(ServerUnavailableException("sin conexión"))

    @Before fun setUp() {
        every { dao.products() } returns flowOf(emptyList())
        repo = CatalogRepository(dao, remote)
    }

    @Test fun seedDemoProducts_insertsSixProductsWhenEmpty() = runTest {
        coEvery { dao.productCount() } returns 0
        repo.seedDemoProducts()
        coVerify { dao.seedProducts(match { it.size == 6 }) }
    }

    @Test fun seedDemoProducts_doesNothingWhenThereAreProducts() = runTest {
        coEvery { dao.productCount() } returns 3
        repo.seedDemoProducts()
        coVerify(exactly = 0) { dao.seedProducts(any()) }
    }

    @Test fun refreshLocalCatalog_returnsTrueOrFalse() = runTest {
        coEvery { dao.productCount() } returns 1
        assertTrue(repo.refreshLocalCatalog())
        coEvery { dao.productCount() } throws RuntimeException("db")
        assertFalse(repo.refreshLocalCatalog())
    }

    @Test fun syncFromServer_savesServerProducts() = runTest {
        coEvery { remote.products() } returns Result.success(listOf(StoreFixture.product("OY009", stock = 4)))
        assertTrue(repo.syncFromServer())
        coVerify { dao.saveProducts(match { it.single().id == "OY009" && it.single().stock == 4 }) }
    }

    @Test fun syncFromServer_fallsBackToLocalSeedWhenServerIsDown() = runTest {
        coEvery { remote.products() } returns down
        coEvery { dao.productCount() } returns 0
        assertFalse(repo.syncFromServer())
        coVerify { dao.seedProducts(any()) }
    }

    @Test fun cartAndClearCartAndUpsert() = runTest {
        every { dao.cartRaw("cliente") } returns flowOf(emptyList())
        repo.cart("cliente")
        repo.clearCart("cliente")
        repo.upsertProduct(StoreFixture.entity())
        coVerify { dao.clearCart("cliente") }
        coVerify { dao.saveProducts(any()) }
    }

    // ---- adjustStock ----
    @Test fun adjustStock_unknownProduct() = runTest {
        coEvery { dao.getProduct("X") } returns null
        assertFalse(repo.adjustStock("X", 1))
    }

    @Test fun adjustStock_usesServerValue() = runTest {
        coEvery { dao.getProduct("OY001") } returns StoreFixture.entity(stock = 10)
        coEvery { remote.adjustStock("OY001", 2) } returns Result.success(StoreFixture.product(stock = 12))
        assertTrue(repo.adjustStock("OY001", 2))
        coVerify { dao.saveProducts(match { it.single().stock == 12 }) }
    }

    @Test fun adjustStock_serverRejectionChangesNothing() = runTest {
        coEvery { dao.getProduct("OY001") } returns StoreFixture.entity(stock = 10)
        coEvery { remote.adjustStock(any(), any()) } returns Result.failure(ApiException(409, "no"))
        assertFalse(repo.adjustStock("OY001", -50))
        coVerify(exactly = 0) { dao.saveProducts(any()) }
    }

    @Test fun adjustStock_offlineAppliesLocallyAndNeverGoesBelowZero() = runTest {
        coEvery { dao.getProduct("OY001") } returns StoreFixture.entity(stock = 10)
        coEvery { remote.adjustStock(any(), any()) } returns down
        assertTrue(repo.adjustStock("OY001", -50))
        coVerify { dao.saveProducts(match { it.single().stock == 0 }) }
    }

    @Test fun adjustStock_noChangeReturnsFalse() = runTest {
        coEvery { dao.getProduct("OY001") } returns StoreFixture.entity(stock = 10)
        coEvery { remote.adjustStock(any(), any()) } returns down
        assertFalse(repo.adjustStock("OY001", 0))
    }

    // ---- changeCart ----
    @Test fun changeCart_ignoresUnknownProduct() = runTest {
        coEvery { dao.getProduct("X") } returns null
        repo.changeCart("cliente", "X", 1)
        coVerify(exactly = 0) { dao.upsertCart(any()) }
    }

    @Test fun changeCart_ignoresAddingInactiveProduct() = runTest {
        coEvery { dao.getProduct("OY001") } returns StoreFixture.entity(active = false)
        repo.changeCart("cliente", "OY001", 1)
        coVerify(exactly = 0) { dao.upsertCart(any()) }
    }

    @Test fun changeCart_addsAndClampsToStock() = runTest {
        coEvery { dao.getProduct("OY001") } returns StoreFixture.entity(stock = 3)
        coEvery { dao.getCartItem("cliente", "OY001") } returns CartItemEntity("cliente", "OY001", 2)
        repo.changeCart("cliente", "OY001", 5)
        coVerify { dao.upsertCart(match { it.quantity == 3 }) }
    }

    @Test fun changeCart_removesWhenQuantityReachesZero() = runTest {
        coEvery { dao.getProduct("OY001") } returns StoreFixture.entity(stock = 3)
        coEvery { dao.getCartItem("cliente", "OY001") } returns CartItemEntity("cliente", "OY001", 1)
        repo.changeCart("cliente", "OY001", -1)
        coVerify { dao.removeCart("cliente", "OY001") }
    }

    @Test fun changeCart_startsFromZeroWhenNotInCart() = runTest {
        coEvery { dao.getProduct("OY001") } returns StoreFixture.entity(stock = 3)
        coEvery { dao.getCartItem(any(), any()) } returns null
        repo.changeCart("cliente", "OY001", 1)
        coVerify { dao.upsertCart(match { it.quantity == 1 }) }
        assertEquals(1, 1)
    }
}
