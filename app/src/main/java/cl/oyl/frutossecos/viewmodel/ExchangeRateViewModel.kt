package cl.oyl.frutossecos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oyl.frutossecos.data.remote.ExchangeRateApi
import cl.oyl.frutossecos.data.remote.RetrofitProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ExchangeUiState {
    data object Loading : ExchangeUiState
    data class Success(val clpPerUsd: Double, val updated: String) : ExchangeUiState
    data class Error(val message: String) : ExchangeUiState
}

/** Consume la API externa (Retrofit) y expone el estado a la interfaz. */
class ExchangeRateViewModel(
    private val api: ExchangeRateApi = RetrofitProvider.exchangeApi
) : ViewModel() {

    private val _state = MutableStateFlow<ExchangeUiState>(ExchangeUiState.Loading)
    val state: StateFlow<ExchangeUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = ExchangeUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                val r = api.latestUsd()
                val clp = r.rates?.get("CLP")
                if (r.result == "success" && clp != null)
                    ExchangeUiState.Success(clp, r.time_last_update_utc.orEmpty())
                else ExchangeUiState.Error("La API no entregó el valor del dólar")
            } catch (e: Exception) {
                ExchangeUiState.Error("No se pudo consultar el dólar")
            }
        }
    }
}
