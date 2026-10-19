package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.domain.model.DemoUser
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(user: DemoUser, onBack: () -> Unit, onLogout: () -> Unit) {
 val repo = (androidx.compose.ui.platform.LocalContext.current.applicationContext as OylApplication).authRepository
 val scope = rememberCoroutineScope()
 var name by remember(user.alias) { mutableStateOf(user.displayName) }
 var email by remember(user.alias) { mutableStateOf("") }
 var phone by remember(user.alias) { mutableStateOf("") }
 var address by remember(user.alias) { mutableStateOf("") }
 var message by remember { mutableStateOf("") }
 val editable = user.alias != "cliente" && user.alias != "admin@admin.cl"
 LaunchedEffect(user.alias) {
  if (editable) repo.getProfile(user.alias)?.let { name = it.name; email = it.email; phone = it.phone; address = it.address }
 }
 Scaffold(topBar = { TopAppBar(title = { Text("Mi perfil") }) }) { padding ->
  Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
   Text("Alias: " + user.alias, style = MaterialTheme.typography.titleMedium)
   if (editable) {
    OutlinedTextField(name, { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(email, {}, label = { Text("Correo") }, readOnly = true, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(phone, { phone = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(address, { address = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth())
    Button(onClick = {
     scope.launch {
      if (name.trim().length < 3) message = "Nombre demasiado corto."
      else try { repo.updateProfile(user.alias, name, phone, address); message = "Perfil actualizado en Room." }
      catch (_: Exception) { message = "Error al guardar." }
     }
    }, modifier = Modifier.fillMaxWidth()) { Text("Guardar perfil") }
   } else Text("Cuenta ficticia preconfigurada. No permite editar datos.")
   if (message.isNotBlank()) Text(message)
   OutlinedButton(onBack, Modifier.fillMaxWidth()) { Text("Volver") }
   TextButton(onLogout) { Text("Cerrar sesión") }
  }
 }
}