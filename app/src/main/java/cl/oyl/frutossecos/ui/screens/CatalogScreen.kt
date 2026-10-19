package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.data.local.ProductEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(ownerAlias: String, isAdmin: Boolean, onBack: () -> Unit, onCart: () -> Unit) {
 val repo = (androidx.compose.ui.platform.LocalContext.current.applicationContext as OylApplication).catalogRepository
 val products by repo.products.collectAsState(initial = emptyList())
 var query by remember { mutableStateOf("") }
 var category by remember { mutableStateOf("Todas") }
 var selected by remember { mutableStateOf<ProductEntity?>(null) }
 val scope = rememberCoroutineScope()
 LaunchedEffect(Unit) { repo.seedDemoProducts() }
 if (selected != null) {
  val item = selected!!
  AlertDialog(onDismissRequest = { selected = null }, title = { Text(item.name) },
   text = { Text(item.description + "\n" + item.presentation + " · $" + item.price + " CLP\nStock DEMO: " + item.stock) },
   confirmButton = {
    if (!isAdmin) TextButton(onClick = { scope.launch { repo.changeCart(ownerAlias, item.id, 1) }; selected = null }) { Text("Agregar al carrito") }
    else TextButton(onClick = { selected = null }) { Text("Cerrar") }
   }, dismissButton = { TextButton(onClick = { selected = null }) { Text("Volver") } })
 }
 Scaffold(topBar = { TopAppBar(title = { Text("Catálogo OYL · DEMO") }) }) { padding ->
  Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
   OutlinedTextField(query, { query = it }, label = { Text("Buscar productos") }, modifier = Modifier.fillMaxWidth())
   Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
    listOf("Todas", "Frutos secos", "Semillas", "Deshidratados").forEach { value ->
     FilterChip(selected = category == value, onClick = { category = value },
      label = { Text(value, style = MaterialTheme.typography.labelSmall) })
    }
   }
   Text("Precios y existencias ficticios. No se consulta el backend.", style = MaterialTheme.typography.bodySmall)
   Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    OutlinedButton(onClick = onBack) { Text("Volver") }
    if (!isAdmin) Button(onClick = onCart) { Text("Mi carrito") }
   }
   LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    items(products.filter { (category == "Todas" || it.category == category) &&
      (it.name.contains(query, true) || it.category.contains(query, true)) }, key = { it.id }) { item ->
     Card(Modifier.fillMaxWidth().clickable { selected = item }) {
      Column(Modifier.padding(14.dp)) {
       Text(item.name, style = MaterialTheme.typography.titleMedium)
       Text(item.category + " · " + item.presentation)
       Text("$" + item.price + " CLP · stock demo: " + item.stock)
       Text("Ver detalle", color = MaterialTheme.colorScheme.primary)
      }
     }
    }
   }
  }
 }
}