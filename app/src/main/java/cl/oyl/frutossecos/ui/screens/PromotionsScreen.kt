package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.oyl.frutossecos.viewmodel.PromotionsViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionsScreen(
    isAdmin: Boolean,
    onBack: () -> Unit,
    onProductClick: (String) -> Unit,
    vm: PromotionsViewModel = viewModel()
) {
    var today by remember {
        mutableStateOf(LocalDate.now())
    }

    // Actualiza la vigencia si la pantalla permanece abierta.
    LaunchedEffect(Unit) {
        while (true) {
            today = LocalDate.now()
            kotlinx.coroutines.delay(60_000)
        }
    }

    val visible = vm.promotions.filter { promotion ->
        if (isAdmin) {
            true
        } else {
            val start = runCatching {
                LocalDate.parse(promotion.startDate)
            }.getOrNull()

            val end = runCatching {
                LocalDate.parse(promotion.endDate)
            }.getOrNull()

            promotion.active &&
                    start != null &&
                    end != null &&
                    !today.isBefore(start) &&
                    !today.isAfter(end) &&
                    vm.products.any {
                        it.id == promotion.productId && it.active
                    }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isAdmin) {
                            "Gestión de Promociones"
                        } else {
                            "Promociones"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = vm::openCreate
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Crear promoción"
                    )
                }
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
            if (vm.loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (vm.message.isNotBlank()) {
                Text(vm.message)
            }

            if (visible.isEmpty() && !vm.loading) {
                Text(
                    if (isAdmin) {
                        "Todavía no hay promociones. Toca + para crear una."
                    } else {
                        "No hay promociones vigentes por ahora."
                    }
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(visible, key = { it.id }) { promotion ->
                    val product = vm.products.find {
                        it.id == promotion.productId
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Image(
                                painter = painterResource(
                                    productImage(promotion.productId)
                                ),
                                contentDescription = product?.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            )

                            Text(
                                text = promotion.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(promotion.description)

                            Text(
                                text = product?.name ?: "Producto no disponible",
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "Desde ${promotion.startDate} hasta ${promotion.endDate}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            if (isAdmin) {
                                val status = when {
                                    !promotion.active -> "Desactivada"
                                    promotion.startDate > today.toString() ->
                                        "Programada"
                                    promotion.endDate < today.toString() ->
                                        "Finalizada"
                                    product?.active != true ->
                                        "Producto no disponible"
                                    else -> "Vigente"
                                }

                                Text(
                                    text = status,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                OutlinedButton(
                                    onClick = {
                                        vm.openEdit(promotion)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Editar promoción")
                                }
                            }

                            Button(
                                onClick = {
                                    onProductClick(promotion.productId)
                                },
                                enabled = product?.active == true,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ver producto")
                            }
                        }
                    }
                }
            }
        }
    }

    if (vm.dialogOpen && isAdmin) {
        var productMenuOpen by remember {
            mutableStateOf(false)
        }

        val form = vm.form

        AlertDialog(
            onDismissRequest = vm::closeDialog,
            title = {
                Text(
                    if (form.id.isBlank()) {
                        "Nueva promoción"
                    } else {
                        "Editar promoción"
                    }
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 460.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PromotionTextField(
                        value = form.title,
                        label = "Título",
                        error = vm.errors["title"],
                        enabled = !vm.saving,
                        onChange = {
                            vm.update(form.copy(title = it))
                        }
                    )

                    PromotionTextField(
                        value = form.description,
                        label = "Descripción",
                        error = vm.errors["description"],
                        enabled = !vm.saving,
                        multiline = true,
                        onChange = {
                            vm.update(form.copy(description = it))
                        }
                    )

                    Box {
                        OutlinedButton(
                            onClick = {
                                productMenuOpen = true
                            },
                            enabled = !vm.saving,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                vm.products.find {
                                    it.id == form.productId
                                }?.name ?: "Seleccionar producto"
                            )
                        }

                        DropdownMenu(
                            expanded = productMenuOpen,
                            onDismissRequest = {
                                productMenuOpen = false
                            }
                        ) {
                            vm.products.forEach { product ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            product.name +
                                                    if (!product.active) {
                                                        " (inactivo)"
                                                    } else {
                                                        ""
                                                    }
                                        )
                                    },
                                    onClick = {
                                        vm.update(
                                            form.copy(productId = product.id)
                                        )
                                        productMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    vm.errors["productId"]?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    PromotionTextField(
                        value = form.startDate,
                        label = "Inicio: AAAA-MM-DD",
                        error = vm.errors["startDate"],
                        enabled = !vm.saving,
                        date = true,
                        onChange = {
                            vm.update(form.copy(startDate = it))
                        }
                    )

                    PromotionTextField(
                        value = form.endDate,
                        label = "Fin: AAAA-MM-DD",
                        error = vm.errors["endDate"],
                        enabled = !vm.saving,
                        date = true,
                        onChange = {
                            vm.update(form.copy(endDate = it))
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Promoción activa")

                        Switch(
                            checked = form.active,
                            enabled = !vm.saving,
                            onCheckedChange = {
                                vm.update(form.copy(active = it))
                            }
                        )
                    }

                    if (vm.formError.isNotBlank()) {
                        Text(
                            text = vm.formError,
                            color = MaterialTheme.colorScheme.error
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
                    onClick = vm::save,
                    enabled = !vm.saving
                ) {
                    Text(
                        if (vm.saving) "Guardando..." else "Guardar"
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = vm::closeDialog,
                    enabled = !vm.saving
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PromotionTextField(
    value: String,
    label: String,
    error: String?,
    enabled: Boolean,
    onChange: (String) -> Unit,
    multiline: Boolean = false,
    date: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = {
            Icon(
                if (date) {
                    Icons.Default.DateRange
                } else {
                    Icons.Default.LocalOffer
                },
                contentDescription = null
            )
        },
        supportingText = {
            if (error != null) Text(error)
        },
        isError = error != null,
        enabled = enabled,
        singleLine = !multiline,
        minLines = if (multiline) 3 else 1,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (date) {
                KeyboardType.Ascii
            } else {
                KeyboardType.Text
            }
        )
    )
}