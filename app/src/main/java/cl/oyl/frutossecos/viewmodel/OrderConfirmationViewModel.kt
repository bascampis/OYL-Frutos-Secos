package cl.oyl.frutossecos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.oyl.frutossecos.data.remote.*
import cl.oyl.frutossecos.data.repository.OrderReceiptStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ConfirmationState(val order: RemoteOrder? = null, val loading: Boolean = true, val error: String? = null)

class OrderConfirmationViewModel(application: Application) : AndroidViewModel(application) {
    private val store = OrderReceiptStore(application)
    private val _state = MutableStateFlow(ConfirmationState())
    val state = _state.asStateFlow()
    private var requested: String? = null

    fun load(owner: String, id: String) {
        if (requested == "$owner:$id") return
        requested = "$owner:$id"
        viewModelScope.launch {
            _state.value = ConfirmationState()
            try {
                val cached = withContext(Dispatchers.IO) { store.read(owner, id) }
                val order = cached ?: OylNetwork.api.orders(owner).firstOrNull { it.id == id && it.clientAlias == owner }
                _state.value = if (order != null) ConfirmationState(order, false)
                    else ConfirmationState(loading = false, error = "No se encontró este pedido para tu cuenta.")
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) {
                _state.value = ConfirmationState(loading = false,
                    error = "No se pudo recuperar el pedido. Consulta Mis pedidos cuando vuelva la conexión.")
            }
        }
    }
}
