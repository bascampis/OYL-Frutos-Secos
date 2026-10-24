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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cl.oyl.frutossecos.data.remote.*
import cl.oyl.frutossecos.ui.components.OylBottomBar
import cl.oyl.frutossecos.ui.theme.OylGreen
import cl.oyl.frutossecos.ui.theme.OylMint
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductManagementScreen(
    navController: NavController,
    currentRoute: String
) {
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var filterTab by remember { mutableStateOf("Todos") }
    var products by remember { mutableStateOf<List<RemoteProduct>>(emptyList()) }
    var message by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    // Form states for new product
    var newId by remember { mutableStateOf("OY007") }
    var newName by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Frutos secos") }
    var newPrice by remember { mutableStateOf("2990") }
    var newStock by remember { mutableStateOf("20") }

    suspend fun refresh() {
        try {
            products = OylNetwork.api.products()
        } catch (_: Exception) {
            message = "Usando caché local o servidor desconectado."
        }
    }

    LaunchedEffect(Unit) { refresh() }

    val filteredProducts = products.filter { p ->
        val matchesSearch = p.name.contains(searchQuery, ignoreCase = true) || p.id.contains(searchQuery, ignoreCase = true)
        val matchesTab = when (filterTab) {
            "Activos" -> p.active && p.stock > 0
            "Sin stock" -> p.stock <= 0
            else -> true
        }
        matchesSearch && matchesTab
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Productos", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { scope.launch { refresh() } }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        },
        bottomBar = {
            OylBottomBar(navController = navController, currentRoute = currentRoute, isAdmin = true)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = OylGreen,
                contentColor = androidx.compose.ui.graphics.Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo producto")
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar producto...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Todos", "Activos", "Sin stock").forEach { tab ->
                    FilterChip(
                        selected = filterTab == tab,
                        onClick = { filterTab = tab },
                        label = { Text(tab) }
                    )
                }
            }

            if (message.isNotBlank()) {
                Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }

            // Products List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
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
                                Text("$${product.price} · Stock: ${product.stock}", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    if (product.active && product.stock > 0) "Activo" else "Inactivo / Sin stock",
                                    color = if (product.stock > 0) OylGreen else MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            IconButton(onClick = {
                                scope.launch {
                                    message = "Editando ${product.name}"
                                }
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = OylGreen)
                            }

                            IconButton(onClick = {
                                scope.launch {
                                    try {
                                        OylNetwork.api.saveProduct(
                                            ProductInputDto(product.id, product.name, product.category, product.description.orEmpty(), product.presentation.orEmpty(), product.price, product.stock, !product.active)
                                        )
                                        refresh()
                                    } catch (_: Exception) { message = "No se pudo actualizar estado." }
                                }
                            }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                            }
                        }
                    }
                }
            }
        }

        // Add Product Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Nuevo Producto") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(newId, { newId = it }, label = { Text("ID (ej: OY008)") })
                        OutlinedTextField(newName, { newName = it }, label = { Text("Nombre") })
                        OutlinedTextField(newCategory, { newCategory = it }, label = { Text("Categoría") })
                        OutlinedTextField(newPrice, { newPrice = it }, label = { Text("Precio CLP") })
                        OutlinedTextField(newStock, { newStock = it }, label = { Text("Stock inicial") })
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        scope.launch {
                            try {
                                OylNetwork.api.saveProduct(
                                    ProductInputDto(newId.trim(), newName.trim(), newCategory.trim(), "Producto natural", "250 g", newPrice.toIntOrNull() ?: 2990, newStock.toIntOrNull() ?: 10, true)
                                )
                                refresh()
                                showAddDialog = false
                                message = "Producto agregado con éxito."
                            } catch (e: Exception) { message = "Error al crear: ${e.localizedMessage}" }
                        }
                    }) { Text("Guardar") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
                }
            )
        }
    }
}
