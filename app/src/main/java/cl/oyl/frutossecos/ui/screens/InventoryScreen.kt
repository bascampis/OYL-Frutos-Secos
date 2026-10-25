package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.data.repository.CatalogRepository
import cl.oyl.frutossecos.ui.components.OylBottomBar
import cl.oyl.frutossecos.ui.theme.OylGreen
import cl.oyl.frutossecos.ui.theme.OylMint
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    navController: NavController,
    currentRoute: String,
    initialQuery: String = ""
) {
    val context = LocalContext.current
    val app = context.applicationContext as OylApplication
    val repo = remember { CatalogRepository(app.database.catalogDao()) }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf(initialQuery) }
    var message by remember { mutableStateOf("") }
    // Cambios preparados por producto, todavía sin confirmar
    val pending = remember { mutableStateMapOf<String, Int>() }
    val products by repo.products.collectAsState(initial = emptyList())

    fun setPending(id: String, value: Int) {
        if (value == 0) pending.remove(id) else pending[id] = value
    }

    val filtered = products.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.id.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Gestión de Inventario", fontWeight = FontWeight.Bold) })
        },
        bottomBar = {
            OylBottomBar(navController = navController, currentRoute = currentRoute, isAdmin = true)
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar producto...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            if (message.isNotBlank()) {
                Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }

            if (filtered.isEmpty()) {
                Text("No se encontraron productos.", style = MaterialTheme.typography.bodyMedium)
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.id }) { product ->
                    val delta = pending[product.id] ?: 0
                    val newStock = product.stock + delta
                    val lowStock = product.stock <= 5

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(OylMint, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🌰", fontSize = 24.sp)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(product.id, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                    Text(
                                        "Stock actual: ${product.stock}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (lowStock) Color(0xFFD32F2F) else OylGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (delta != 0) {
                                        val sign = if (delta > 0) "+" else ""
                                        Text(
                                            "Cambio pendiente: $sign$delta → nuevo stock $newStock",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Text("Stock mínimo: 5 un", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    FilledIconButton(
                                        onClick = { setPending(product.id, delta - 1) },
                                        enabled = newStock > 0,
                                        modifier = Modifier.size(40.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = Color(0xFFD32F2F),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Salida")
                                    }
                                    FilledIconButton(
                                        onClick = { setPending(product.id, delta + 1) },
                                        modifier = Modifier.size(40.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = OylGreen,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Entrada")
                                    }
                                }
                            }

                            if (delta != 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { pending.remove(product.id) },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Cancelar") }
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                val ok = repo.adjustStock(product.id, delta)
                                                pending.remove(product.id)
                                                message = if (ok) "Inventario actualizado: ${product.name} ahora tiene $newStock un."
                                                else "No se pudo actualizar el inventario de ${product.name}."
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = OylGreen)
                                    ) { Text("Confirmar", fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}