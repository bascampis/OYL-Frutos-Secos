package cl.oyl.frutossecos.ui.screens

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
fun CartScreen(ownerAlias: String, onBack: () -> Unit) {
 val db = (androidx.compose.ui.platform.LocalContext.current.applicationContext as OylApplication).database
 val repo = (androidx.compose.ui.platform.LocalContext.current.applicationContext as OylApplication).catalogRepository
 val cart by repo.cart(ownerAlias).collectAsState(initial = emptyList())
 val products by repo.products.collectAsState(initial = emptyList())
 val lookup = products.associateBy { it.id }
 val scope = rememberCoroutineScope()
 val total = cart.sumOf { (lookup[it.productId]?.price ?: 0) * it.quantity }
 Scaffold(topBar = { TopAppBar(title = { Text("Mi carrito · DEMO") }) }) { padding ->
  Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
   Text("Carrito local separado por cliente. No hay compra ni reserva de stock.")
   LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    items(cart, key = { it.productId }) { line ->
     val product = lookup[line.productId]
     if (product != null) Card(Modifier.fillMaxWidth()) {
      Column(Modifier.padding(12.dp)) {
       Text(product.name, style = MaterialTheme.typography.titleMedium)
       Text("$" + product.price + " × " + line.quantity)
       Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { scope.launch { repo.changeCart(ownerAlias, line.productId, -1) } }) { Text("−") }
        OutlinedButton(onClick = { scope.launch { repo.changeCart(ownerAlias, line.productId, 1) } }) { Text("+") }
        TextButton(onClick = { scope.launch { repo.changeCart(ownerAlias, line.productId, -line.quantity) } }) { Text("Quitar") }
       }
      }
     }
    }
   }
   Text("Total referencial: $" + total + " CLP", style = MaterialTheme.typography.titleLarge)
   Text("Confirmación de pedidos disponible cuando se integre el servidor.", color = MaterialTheme.colorScheme.secondary)
   Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver al catálogo") }
  }
 }
}