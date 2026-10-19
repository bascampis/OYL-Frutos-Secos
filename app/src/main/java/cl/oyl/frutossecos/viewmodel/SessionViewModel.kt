package cl.oyl.frutossecos.viewmodel

import androidx.lifecycle.ViewModel
import cl.oyl.frutossecos.domain.model.DemoUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** La sesión es solo en memoria en ZIP 01. En ZIP 02 se persistirá localmente. */
class SessionViewModel : ViewModel() {
    private val _user = MutableStateFlow<DemoUser?>(null)
    val user: StateFlow<DemoUser?> = _user.asStateFlow()
    fun startSession(user: DemoUser) { _user.value = user }
    fun endSession() { _user.value = null }
}
