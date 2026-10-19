package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cl.oyl.frutossecos.domain.model.DemoUser
import cl.oyl.frutossecos.viewmodel.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(vm: RegisterViewModel, onBack: () -> Unit, onDone: (DemoUser) -> Unit) {
 val s by vm.state.collectAsState()
 Scaffold(topBar = { TopAppBar(title = { Text("Registro de cliente") }) }) { p ->
  Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState()).padding(20.dp),
   verticalArrangement = Arrangement.spacedBy(12.dp)) {
   Text("Crea tu cuenta de demostración", style = MaterialTheme.typography.headlineSmall)
   OutlinedTextField(s.name, { vm.edit("name", it) }, Modifier.fillMaxWidth(), label = { Text("Nombre completo *") }, singleLine = true)
   OutlinedTextField(s.alias, { vm.edit("alias", it) }, Modifier.fillMaxWidth(), label = { Text("Alias *") }, singleLine = true)
   OutlinedTextField(s.email, { vm.edit("email", it) }, Modifier.fillMaxWidth(), label = { Text("Correo de prueba *") }, singleLine = true)
   OutlinedTextField(s.phone, { vm.edit("phone", it) }, Modifier.fillMaxWidth(), label = { Text("Teléfono") }, singleLine = true)
   OutlinedTextField(s.address, { vm.edit("address", it) }, Modifier.fillMaxWidth(), label = { Text("Dirección") })
   OutlinedTextField(s.password, { vm.edit("password", it) }, Modifier.fillMaxWidth(), label = { Text("Contraseña *") },
    visualTransformation = PasswordVisualTransformation(), singleLine = true)
   if (s.error != null) Text(s.error.orEmpty(), color = MaterialTheme.colorScheme.error)
   Button(onClick = { vm.submit(onDone) }, enabled = !s.busy, modifier = Modifier.fillMaxWidth()) {
    Text(if (s.busy) "Guardando..." else "Crear cuenta")
   }
   OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
  }
 }
}