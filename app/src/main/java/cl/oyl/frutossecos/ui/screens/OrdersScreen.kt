package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.oyl.frutossecos.data.remote.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(alias: String, onBack: () -> Unit) {
 var orders by remember { mutableStateOf<List<RemoteOrder>>(emptyList()) }
 var error by remember { mutableStateOf("") }
 var refresh by remember { mutableIntStateOf(0) }
 LaunchedEffect(alias, refresh) {
  try { orders = OylNetwork.api.orders(alias); error = "" }
  catch (_: Exception) { error = "No es posible consultar pedidos: servidor desconectado." }
 }
 Scaffold(topBar = { TopAppBar(title = { Text("Historial de pedidos") }) }) { pad ->
  Column(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
   Text("Pedidos registrados en el backend académico.")
   Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    OutlinedButton(onClick = onBack) { Text("Volver") }
    Button(onClick = { refresh += 1 }) { Text("Actualizar") }
   }
   if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
   if (orders.isEmpty() && error.isBlank()) Text("Sin solicitudes registradas.")
   LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    items(orders, key = { it.id }) { order ->
     Card(Modifier.fillMaxWidth()) {
      Column(Modifier.padding(14.dp)) {
       Text("Pedido " + order.id.take(8), style = MaterialTheme.typography.titleMedium)
       Text(order.status)
       Text("$" + order.total + " CLP · " + order.fulfillment)
       Text(order.lines.joinToString { it.productName + " × " + it.quantity })
      }
     }
    }
   }
  }
 }
}