package cl.oyl.frutossecos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.data.repository.DemoAuthRepository
import cl.oyl.frutossecos.domain.model.*
import cl.oyl.frutossecos.domain.validation.LoginValidator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LoginUiState(val role: UserRole = UserRole.CLIENTE, val alias: String = "", val password: String = "",
 val aliasError: String? = null, val passwordError: String? = null, val generalError: String? = null,
 val isLoading: Boolean = false)

class LoginViewModel(application: Application): AndroidViewModel(application) {
 private val repo = (application as OylApplication).authRepository
 private val _uiState = MutableStateFlow(LoginUiState())
 val uiState = _uiState.asStateFlow()
 fun selectRole(role: UserRole) = _uiState.update { it.copy(role = role, alias = "", password = "", aliasError = null, passwordError = null, generalError = null) }
 fun updateAlias(value: String) = _uiState.update { it.copy(alias = value, aliasError = null, generalError = null) }
 fun updatePassword(value: String) = _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }
 fun useDemoCredentials() = _uiState.update { it.copy(alias = if (it.role == UserRole.CLIENTE) "cliente" else "admin@admin.cl",
  password = DemoAuthRepository.DEMO_PASSWORD, aliasError = null, passwordError = null, generalError = null) }
 fun signIn(onSuccess: (DemoUser) -> Unit) {
  val s = _uiState.value
  if (s.isLoading) return
  val e = LoginValidator.validate(s.alias, s.password)
  if (!e.isValid) { _uiState.update { it.copy(aliasError = e.alias, passwordError = e.password) }; return }
  viewModelScope.launch {
   _uiState.update { it.copy(isLoading = true, generalError = null) }
   try {
    val u = repo.signIn(s.role, s.alias, s.password)
    if (u != null) onSuccess(u) else _uiState.update { it.copy(generalError = "Credenciales o rol incorrectos.") }
   } catch (_: Exception) { _uiState.update { it.copy(generalError = "Error al consultar datos locales.") } }
   finally { _uiState.update { it.copy(isLoading = false) } }
  }
 }
}