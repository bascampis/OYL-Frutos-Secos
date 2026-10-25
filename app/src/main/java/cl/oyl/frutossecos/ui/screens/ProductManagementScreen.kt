package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cl.oyl.frutossecos.R
import cl.oyl.frutossecos.data.remote.*
import cl.oyl.frutossecos.ui.components.OylBottomBar
import cl.oyl.frutossecos.ui.theme.OylGreen
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
    var products by remember {
        mutableStateOf<List<RemoteProduct>>(emptyList())
    }
    var message by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    var newId by remember { mutableStateOf("OY007") }
    var newName by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Frutos secos") }
    var newPrice by remember { mutableStateOf("2990") }
    var newStock by remember { mutableStateOf("20") }

    suspend fun refresh() {
        try {
            products = OylNetwork.api.products()
        } catch (_: Exception) {
            message = "No se pudieron actualizar los productos. Comprueba el backend."
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    val filteredProducts = products.filter { product ->
        val matchesSearch =
            product.name.contains(searchQuery, ignoreCase = true) ||
                    product.id.contains(searchQuery, ignoreCase = true)

        val matchesTab = when (filterTab) {
            "Activos" -> product.active && product.stock > 0
            "Sin stock" -> product.stock <= 0
            else -> true
        }

        matchesSearch && matchesTab
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gestión de Productos",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                refresh()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                }
            )
        },
        bottomBar = {
            OylBottomBar(
                navController = navController,
                currentRoute = currentRoute,
                isAdmin = true
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddDialog = true
                },
                containerColor = OylGreen,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nuevo producto"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Buscar producto...")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Todos", "Activos", "Sin stock").forEach { tab ->
                    FilterChip(
                        selected = filterTab == tab,
                        onClick = {
                            filterTab = tab
                        },
                        label = {
                            Text(tab)
                        }
                    )
                }
            }

            if (message.isNotBlank()) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(
                    items = filteredProducts,
                    key = { it.id }
                ) { product ->
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
                            Image(
                                painter = painterResource(
                                    id = managementProductImage(product.id)
                                ),
                                contentDescription = product.name,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = product.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "$${product.price} · Stock: ${product.stock}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = if (product.active && product.stock > 0) {
                                        "Activo"
                                    } else {
                                        "Inactivo / Sin stock"
                                    },
                                    color = if (product.active && product.stock > 0) {
                                        OylGreen
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    },
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            IconButton(
                                onClick = {
                                    message = "Editando ${product.name}"
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = OylGreen
                                )
                            }

                            IconButton(
                                onClick = {
                                    scope.launch {
                                        try {
                                            OylNetwork.api.saveProduct(
                                                ProductInputDto(
                                                    product.id,
                                                    product.name,
                                                    product.category,
                                                    product.description.orEmpty(),
                                                    product.presentation.orEmpty(),
                                                    product.price,
                                                    product.stock,
                                                    !product.active
                                                )
                                            )

                                            refresh()
                                        } catch (_: Exception) {
                                            message = "No se pudo actualizar estado."
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Cambiar estado del producto"
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                },
                title = {
                    Text("Nuevo Producto")
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newId,
                            onValueChange = {
                                newId = it
                            },
                            label = {
                                Text("ID (ej: OY008)")
                            }
                        )

                        OutlinedTextField(
                            value = newName,
                            onValueChange = {
                                newName = it
                            },
                            label = {
                                Text("Nombre")
                            }
                        )

                        OutlinedTextField(
                            value = newCategory,
                            onValueChange = {
                                newCategory = it
                            },
                            label = {
                                Text("Categoría")
                            }
                        )

                        OutlinedTextField(
                            value = newPrice,
                            onValueChange = {
                                newPrice = it
                            },
                            label = {
                                Text("Precio CLP")
                            }
                        )

                        OutlinedTextField(
                            value = newStock,
                            onValueChange = {
                                newStock = it
                            },
                            label = {
                                Text("Stock inicial")
                            }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    OylNetwork.api.saveProduct(
                                        ProductInputDto(
                                            newId.trim(),
                                            newName.trim(),
                                            newCategory.trim(),
                                            "Producto natural",
                                            "250 g",
                                            newPrice.toIntOrNull() ?: 2990,
                                            newStock.toIntOrNull() ?: 10,
                                            true
                                        )
                                    )

                                    refresh()
                                    showAddDialog = false
                                    message = "Producto agregado con éxito."
                                } catch (e: Exception) {
                                    message = "Error al crear: ${e.localizedMessage}"
                                }
                            }
                        }
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddDialog = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

private fun managementProductImage(productId: String): Int {
    return when (productId) {
        "OY001" -> R.drawable.almendras_inv
        "OY002" -> R.drawable.nueces_inv
        "OY003" -> R.drawable.mixpremium_inv
        "OY004" -> R.drawable.pasasmorenas_inv
        "OY005" -> R.drawable.semillaschia_inv
        "OY006" -> R.drawable.cranberries_inv
        else -> R.drawable.frutos_imagen
    }
}