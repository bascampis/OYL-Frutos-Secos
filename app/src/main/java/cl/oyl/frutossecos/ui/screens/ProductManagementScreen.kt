package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cl.oyl.frutossecos.R
import cl.oyl.frutossecos.ui.components.OylBottomBar
import cl.oyl.frutossecos.ui.theme.OylGreen
import cl.oyl.frutossecos.viewmodel.ProductManagementViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductManagementScreen(
    navController: NavController,
    currentRoute: String,
    vm: ProductManagementViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterTab by remember { mutableStateOf("Todos") }

    val busy = vm.loading || vm.saving

    val filteredProducts = vm.products.filter { product ->
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
                        onClick = { vm.refresh() },
                        enabled = !busy
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
                    if (!busy) vm.openCreate()
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
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar producto...") },
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

            if (busy) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (vm.message.isNotBlank()) {
                Text(
                    text = vm.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (filteredProducts.isEmpty() && !busy) {
                Text("No se encontraron productos.")
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val fallback = painterResource(managementProductImage(product.id))
                            if (product.imageUrl.isNullOrBlank()) {
                                Image(
                                    painter = fallback,
                                    contentDescription = product.name,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                AsyncImage(
                                    model = product.imageUrl,
                                    contentDescription = product.name,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop,
                                    placeholder = fallback,
                                    error = fallback
                                )
                            }

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
                                    text = product.presentation.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Text(
                                    text = when {
                                        !product.active -> "Inactivo"
                                        product.stock <= 0 -> "Sin stock"
                                        else -> "Activo"
                                    },
                                    color = if (
                                        product.active && product.stock > 0
                                    ) {
                                        OylGreen
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    },
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            IconButton(
                                onClick = { vm.openEdit(product) },
                                enabled = !busy
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar ${product.name}",
                                    tint = OylGreen
                                )
                            }

                            IconButton(
                                onClick = { vm.toggleActive(product) },
                                enabled = !busy
                            ) {
                                Icon(
                                    imageVector = if (product.active) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                    contentDescription = if (product.active) {
                                        "Desactivar ${product.name}"
                                    } else {
                                        "Activar ${product.name}"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (vm.dialogOpen) {
        AlertDialog(
            onDismissRequest = { vm.closeDialog() },
            title = {
                Text(
                    if (vm.editing) {
                        "Editar producto"
                    } else {
                        "Nuevo producto"
                    }
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProductEditorField(
                        value = vm.form.id,
                        label = "Identificador",
                        icon = Icons.Default.Tag,
                        error = vm.errors["id"],
                        enabled = !vm.saving,
                        readOnly = vm.editing,
                        onChange = { vm.updateField("id", it) }
                    )

                    ProductEditorField(
                        value = vm.form.name,
                        label = "Nombre",
                        icon = Icons.Default.ShoppingBag,
                        error = vm.errors["name"],
                        enabled = !vm.saving,
                        onChange = { vm.updateField("name", it) }
                    )

                    ProductEditorField(
                        value = vm.form.category,
                        label = "Categoría",
                        icon = Icons.Default.Category,
                        error = vm.errors["category"],
                        enabled = !vm.saving,
                        onChange = { vm.updateField("category", it) }
                    )

                    ProductEditorField(
                        value = vm.form.description,
                        label = "Descripción",
                        icon = Icons.Default.Description,
                        error = vm.errors["description"],
                        enabled = !vm.saving,
                        singleLine = false,
                        onChange = { vm.updateField("description", it) }
                    )

                    ProductEditorField(
                        value = vm.form.presentation,
                        label = "Presentación, por ejemplo 250 g",
                        icon = Icons.Default.Inventory2,
                        error = vm.errors["presentation"],
                        enabled = !vm.saving,
                        onChange = { vm.updateField("presentation", it) }
                    )

                    ProductEditorField(
                        value = vm.form.price,
                        label = "Precio CLP",
                        icon = Icons.Default.AttachMoney,
                        error = vm.errors["price"],
                        enabled = !vm.saving,
                        numeric = true,
                        onChange = { vm.updateField("price", it) }
                    )
                    ProductEditorField(
                        value = vm.form.imageUrl,
                        label = "URL de la imagen (opcional)",
                        icon = Icons.Default.Link,
                        error = vm.errors["imageUrl"],
                        enabled = !vm.saving,
                        onChange = { vm.updateField("imageUrl", it) }
                    )

                    if (vm.form.imageUrl.trim().startsWith("https://") && vm.errors["imageUrl"] == null) {
                        AsyncImage(
                            model = vm.form.imageUrl.trim(),
                            contentDescription = "Vista previa de la imagen",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    if (!vm.editing) {
                        ProductEditorField(
                            value = vm.form.stock,
                            label = "Stock inicial",
                            icon = Icons.Default.Numbers,
                            error = vm.errors["stock"],
                            enabled = !vm.saving,
                            numeric = true,
                            onChange = { vm.updateField("stock", it) }
                        )
                    } else {
                        Text(
                            text = "Las entradas y salidas de stock se registran en Gestión de Inventario.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (vm.formError.isNotBlank()) {
                        Text(
                            text = vm.formError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (vm.saving) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { vm.save() },
                    enabled = !vm.saving
                ) {
                    Text(
                        if (vm.saving) {
                            "Guardando..."
                        } else {
                            "Guardar"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { vm.closeDialog() },
                    enabled = !vm.saving
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ProductEditorField(
    value: String,
    label: String,
    icon: ImageVector,
    error: String?,
    enabled: Boolean,
    onChange: (String) -> Unit,
    readOnly: Boolean = false,
    numeric: Boolean = false,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(error)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (numeric) {
                KeyboardType.Number
            } else {
                KeyboardType.Text
            }
        )
    )
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