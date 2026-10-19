package cl.oyl.frutossecos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oyl.frutossecos.data.repository.AuthRepository
import cl.oyl.frutossecos.data.repository.DemoAuthRepository
import cl.oyl.frutossecos.domain.model.DemoUser
import cl.oyl.frutossecos.domain.model.UserRole
import cl.oyl.frutossecos.domain.validation.LoginValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val role: UserRole = UserRole.CLIENTE,
    val alias: String = "",
    val password: String = "",
    val aliasError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false
)

class LoginViewModel : ViewModel() {
    private val authRepository: AuthRepository = DemoAuthRepository()
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun selectRole(role: UserRole) = _uiState.update {
        it.copy(role = role, alias = "", password = "", aliasError = null, passwordError = null, generalError = null)
    }

    fun updateAlias(value: String) = _uiState.update {
        it.copy(alias = value, aliasError = null, generalError = null)
    }

    fun updatePassword(value: String) = _uiState.update {
        it.copy(password = value, passwordError = null, generalError = null)
    }

    fun useDemoCredentials() = _uiState.update {
        it.copy(
            alias = if (it.role == UserRole.ADMINISTRADOR) "admin@admin.cl" else "cliente",
            password = DemoAuthRepository.DEMO_PASSWORD,
            aliasError = null,
            passwordError = null,
            generalError = null
        )
    }

    fun signIn(onSuccess: (DemoUser) -> Unit) {
        val state = _uiState.value
        if (state.isLoading) return
        val errors = LoginValidator.validate(state.alias, state.password)
        if (!errors.isValid) {
            _uiState.update { it.copy(aliasError = errors.alias, passwordError = errors.password, generalError = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            try {
                val result = authRepository.signIn(state.role, state.alias, state.password)
                if (result != null) onSuccess(result)
                else _uiState.update { it.copy(generalError = "Alias, contraseña o rol incorrectos.") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
