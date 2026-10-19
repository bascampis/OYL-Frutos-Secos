package cl.oyl.frutossecos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.oyl.frutossecos.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Sesión local de demo: no almacena claves. */
class SessionViewModel(application: Application): AndroidViewModel(application) {
 private val prefs = application.getSharedPreferences("oyl_session", 0)
 private val _user = MutableStateFlow(prefs.getString("alias", null)?.let { alias ->
  DemoUser(alias, prefs.getString("name", alias) ?: alias,
   if (prefs.getString("role", "") == UserRole.ADMINISTRADOR.name) UserRole.ADMINISTRADOR else UserRole.CLIENTE)
 })
 val user = _user.asStateFlow()
 fun startSession(user: DemoUser) {
  prefs.edit().putString("alias", user.alias).putString("name", user.displayName).putString("role", user.role.name).apply()
  _user.value = user
 }
 fun endSession() { prefs.edit().clear().apply(); _user.value = null }
}