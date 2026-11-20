package cl.oyl.frutossecos.viewmodel

import cl.oyl.frutossecos.data.remote.ExchangeRateApi
import cl.oyl.frutossecos.data.remote.ExchangeRateResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExchangeRateViewModelTest {

    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun loadsRateFromExternalApi() {
        val api = mockk<ExchangeRateApi>()
        coEvery { api.latestUsd() } returns
            ExchangeRateResponse("success", "USD", "Wed, 18 Nov 2026", mapOf("CLP" to 950.5))
        val vm = ExchangeRateViewModel(api)
        val s = vm.state.value
        assertTrue(s is ExchangeUiState.Success)
        assertEquals(950.5, (s as ExchangeUiState.Success).clpPerUsd, 0.0001)
    }

    @Test fun showsErrorWhenResponseHasNoClp() {
        val api = mockk<ExchangeRateApi>()
        coEvery { api.latestUsd() } returns ExchangeRateResponse("success", "USD", null, mapOf("EUR" to 0.9))
        assertTrue(ExchangeRateViewModel(api).state.value is ExchangeUiState.Error)
    }

    @Test fun showsErrorWhenResultIsNotSuccess() {
        val api = mockk<ExchangeRateApi>()
        coEvery { api.latestUsd() } returns ExchangeRateResponse("error", null, null, null)
        assertTrue(ExchangeRateViewModel(api).state.value is ExchangeUiState.Error)
    }

    @Test fun showsErrorWhenNetworkFails() {
        val api = mockk<ExchangeRateApi>()
        coEvery { api.latestUsd() } throws java.io.IOException("sin internet")
        assertTrue(ExchangeRateViewModel(api).state.value is ExchangeUiState.Error)
    }

    @Test fun retryRecoversAfterFailure() {
        val api = mockk<ExchangeRateApi>()
        coEvery { api.latestUsd() } throws java.io.IOException("x")
        val vm = ExchangeRateViewModel(api)
        coEvery { api.latestUsd() } returns ExchangeRateResponse("success", "USD", "", mapOf("CLP" to 900.0))
        vm.load()
        assertTrue(vm.state.value is ExchangeUiState.Success)
    }
}
