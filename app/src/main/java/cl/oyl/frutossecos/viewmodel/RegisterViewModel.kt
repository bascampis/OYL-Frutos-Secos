package cl.oyl.frutossecos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.domain.model.DemoUser
import cl.oyl.frutossecos.domain.validation.RegisterValidator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RegisterState(val name: String = "", val alias: String = "", val email: String = "",
                         val phone: String = "", val address: String = "", val password: String = "", val confirm: String = "",
                         val error: String? = null, val busy: Boolean = false)

class RegisterViewModel(application: Application): AndroidViewModel(application) {
 private val repo = (application as OylApplication).authRepository
 private val _state = MutableStateFlow(RegisterState())
 val state = _state.asStateFlow()
 fun edit(field: String, v: String) = _state.update {
  when (field) {
   "name" -> it.copy(name = v, error = null)
   "alias" -> it.copy(alias = v, error = null)
   "email" -> it.copy(email = v, error = null)
   "phone" -> it.copy(phone = v, error = null)
   "address" -> it.copy(address = v, error = null)
   "confirm" -> it.copy(confirm = v, error = null)
   else -> it.copy(password = v, error = null)
  }
 }
 fun submit(onSuccess: (DemoUser) -> Unit) {
  val s = _state.value
  if (s.busy) return
  val email = s.email.trim().lowercase()
  val errors = RegisterValidator.validate(s.name, email, s.phone, s.address, s.password, s.confirm)
  if (!errors.isValid) {
   _state.update { it.copy(error = "Revisa los campos marcados en rojo.") }
   return
  }
  viewModelScope.launch {
   _state.update { it.copy(busy = true, error = null) }
   try { onSuccess(repo.register(s.name, email, email, s.phone, s.address, s.password)) }
   catch (e: Exception) { _state.update { it.copy(error = e.message ?: "No se pudo registrar.") } }
   finally { _state.update { it.copy(busy = false) } }
  }
 }
}