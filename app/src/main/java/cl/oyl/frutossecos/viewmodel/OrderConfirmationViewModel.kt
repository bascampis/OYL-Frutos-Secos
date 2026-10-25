package cl.oyl.frutossecos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.oyl.frutossecos.data.model.LocalOrder
import cl.oyl.frutossecos.data.repository.LocalStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConfirmationState(
    val order: LocalOrder? = null,
    val loading: Boolean = true,
    val error: String? = null
)

class OrderConfirmationViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(
        ConfirmationState()
    )

    val state = _state.asStateFlow()

    private var requested: String? = null

    fun load(
        owner: String,
        id: String
    ) {
        val requestKey = "$owner:$id"

        if (requested == requestKey) return

        requested = requestKey

        viewModelScope.launch {
            _state.value = ConfirmationState()

            try {
                val order = LocalStore.repository
                    .orders(owner)
                    .firstOrNull {
                        it.id == id &&
                                it.clientAlias == owner
                    }

                _state.value = if (order != null) {
                    ConfirmationState(
                        order = order,
                        loading = false
                    )
                } else {
                    ConfirmationState(
                        loading = false,
                        error = "No se encontró este pedido para tu cuenta."
                    )
                }
            } catch (e: CancellationException) {
                requested = null
                throw e
            } catch (_: Exception) {
                requested = null

                _state.value = ConfirmationState(
                    loading = false,
                    error = "No se pudo recuperar el pedido guardado en el celular."
                )
            }
        }
    }
}