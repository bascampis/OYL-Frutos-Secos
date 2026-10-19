package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.oyl.frutossecos.domain.model.*
import cl.oyl.frutossecos.ui.components.FeatureTile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(user: DemoUser, onAbout: () -> Unit, onLogout: () -> Unit, onProfile: () -> Unit, onCatalog: () -> Unit, onCart: () -> Unit, onQr: () -> Unit, onMap: () -> Unit, onOrders: () -> Unit, onAdmin: () -> Unit, onNotifications: () -> Unit) {
 val admin = user.role == UserRole.ADMINISTRADOR
 Scaffold(topBar = { TopAppBar(title = { Text("OYL Frutos Secos") }, actions = {
  IconButton(onClick = onLogout) { Icon(Icons.Outlined.Logout, "Cerrar sesión") }
 }) }) { p ->
  Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState()).padding(20.dp),
   verticalArrangement = Arrangement.spacedBy(14.dp)) {
   Text("¡Hola, " + user.displayName + "!", style = MaterialTheme.typography.headlineMedium)
   Text(if (admin) "Panel de administración · Demostración" else "Panel de cliente · Demostración")
   Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
    Column(Modifier.padding(20.dp)) {
     Text("Sesión local activa", style = MaterialTheme.typography.titleMedium)
     Text("Rol: " + if (admin) "Administrador" else "Cliente")
     Text("La versión actual no registra compras reales.")
    }
   }
   Button(onClick = onProfile, modifier = Modifier.fillMaxWidth()) { Text("Mi perfil") }
   Button(onClick = onCatalog, modifier = Modifier.fillMaxWidth()) { Text("Abrir catálogo de demostración") }
   if (!admin) Button(onClick = onCart, modifier = Modifier.fillMaxWidth()) { Text("Mi carrito") }
   Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    OutlinedButton(onClick = onQr, modifier = Modifier.weight(1f)) { Text("Escanear QR") }
    OutlinedButton(onClick = onMap, modifier = Modifier.weight(1f)) { Text("Ubicación") }
   }
   if (!admin) Button(onClick = onOrders, modifier = Modifier.fillMaxWidth()) { Text("Mis pedidos del servidor") }
   if (admin) Button(onClick = onAdmin, modifier = Modifier.fillMaxWidth()) { Text("Gestión de productos, stock y pedidos") }
   OutlinedButton(onClick = onNotifications, modifier = Modifier.fillMaxWidth()) { Text("Notificaciones") }
   Text("Próximos módulos", style = MaterialTheme.typography.titleLarge)
   if (admin) {
    FeatureTile("Productos", "Edición de catálogo", "Siguiente fase")
    FeatureTile("Inventario", "Control de existencias", "Siguiente fase")
    FeatureTile("Pedidos", "Gestión de solicitudes", "Siguiente fase")
   } else {
    FeatureTile("Catálogo", "Productos y filtros", "Siguiente fase")
    FeatureTile("Carrito", "Por cliente", "Siguiente fase")
    FeatureTile("Mapa y QR", "Recursos nativos", "Siguiente fase")
   }
   OutlinedButton(onAbout, Modifier.fillMaxWidth()) { Text("Acerca de") }
   OutlinedButton(onLogout, Modifier.fillMaxWidth()) { Text("Salir") }
  }
 }
}