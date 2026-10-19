package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.oyl.frutossecos.data.remote.*
import kotlinx.coroutines.launch

/** Panel administrativo SOLO para backend local de demostración, sin autorización remota. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onBack: () -> Unit) {
 val scope = rememberCoroutineScope()
 var products by remember { mutableStateOf<List<RemoteProduct>>(emptyList()) }
 var orders by remember { mutableStateOf<List<RemoteOrder>>(emptyList()) }
 var selected by remember { mutableStateOf("") }
 var name by remember { mutableStateOf("") }
 var category by remember { mutableStateOf("Frutos secos") }
 var price by remember { mutableStateOf("0") }
 var stock by remember { mutableStateOf("0") }
 var change by remember { mutableStateOf("1") }
 var message by remember { mutableStateOf("Conecta Spring Boot en tu computador.") }
 var busy by remember { mutableStateOf(false) }
 suspend fun refresh() {
  try {
   products = OylNetwork.api.products()
   orders = OylNetwork.api.allOrders()
   message = "Datos recuperados del servidor."
  } catch (_: Exception) { message = "No hay conexión con Spring Boot." }
 }
 LaunchedEffect(Unit) { refresh() }
 Scaffold(topBar = { TopAppBar(title = { Text("Administración · DEMO") }) }) { pad ->
  Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
   verticalArrangement = Arrangement.spacedBy(10.dp)) {
   Text("Gestión de productos e inventario", style = MaterialTheme.typography.titleLarge)
   Text("Acceso local académico SIN seguridad remota. No utilizar con datos reales.", color = MaterialTheme.colorScheme.error)
   Text(message)
   OutlinedButton(onBack) { Text("Volver") }
   Button(onClick = { scope.launch { refresh() } }) { Text("Actualizar desde servidor") }
   HorizontalDivider()
   Text("Selecciona producto para editar", style = MaterialTheme.typography.titleMedium)
   products.forEach { product ->
    TextButton(onClick = {
     selected = product.id; name = product.name; category = product.category
     price = product.price.toString(); stock = product.stock.toString()
    }) { Text(product.id + " · " + product.name + " · stock " + product.stock) }
   }
   OutlinedTextField(selected, { selected = it }, label = { Text("ID (nuevo: OY007)") }, modifier = Modifier.fillMaxWidth())
   OutlinedTextField(name, { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
   OutlinedTextField(category, { category = it }, label = { Text("Categoría") }, modifier = Modifier.fillMaxWidth())
   OutlinedTextField(price, { price = it }, label = { Text("Precio CLP") }, modifier = Modifier.fillMaxWidth())
   OutlinedTextField(stock, { stock = it }, label = { Text("Stock inicial (solo nuevos)") }, modifier = Modifier.fillMaxWidth())
   Button(onClick = {
    scope.launch {
     busy = true
     try {
      OylNetwork.api.saveProduct(ProductInputDto(selected.trim(),name.trim(),category.trim(),
       "", "250 g",price.toIntOrNull() ?: -1,stock.toIntOrNull() ?: -1,true))
      refresh()
      message = "Producto guardado en backend (stock de existentes no se reemplaza)."
     } catch (e: Exception) { message = "Error al guardar: " + (e.localizedMessage ?: "") }
     finally { busy = false }
    }
   }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Guardar producto en servidor") }
   HorizontalDivider()
   Text("Movimiento de inventario", style = MaterialTheme.typography.titleMedium)
   Text("Producto elegido: " + selected)
   OutlinedTextField(change, { change = it }, label = { Text("Cantidad positiva") }, modifier = Modifier.fillMaxWidth())
   Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    Button(onClick = {
     scope.launch {
      try {
       val qty = change.toIntOrNull() ?: 0
       require(qty > 0)
       OylNetwork.api.adjustStock(selected, StockDeltaDto(qty)); refresh()
      } catch (_: Exception) { message = "No se pudo sumar stock." }
     }
    }) { Text("Entrada +") }
    OutlinedButton(onClick = {
     scope.launch {
      try {
       val qty = change.toIntOrNull() ?: 0
       require(qty > 0)
       OylNetwork.api.adjustStock(selected, StockDeltaDto(-qty)); refresh()
      } catch (_: Exception) { message = "No se pudo descontar stock." }
     }
    }) { Text("Salida −") }
   }
   HorizontalDivider()
   Text("Solicitudes recibidas", style = MaterialTheme.typography.titleLarge)
   orders.forEach { order ->
    Card(Modifier.fillMaxWidth()) {
     Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text("Pedido " + order.id.take(8) + " · " + order.clientAlias)
      Text(order.status + " · $" + order.total)
      val next = when(order.status) {
       "Pendiente de Validación" -> "Confirmado"
       "Confirmado" -> "Preparado"
       "Preparado" -> "Entregado"
       else -> null
      }
      if (next != null) Button(onClick = {
       scope.launch {
        try { OylNetwork.api.setStatus(order.id,OrderStatusDto(next)); refresh() }
        catch (_: Exception) { message = "No se pudo cambiar estado." }
       }
      }) { Text("Cambiar a " + next) }
     }
    }
   }
  }
 }
}