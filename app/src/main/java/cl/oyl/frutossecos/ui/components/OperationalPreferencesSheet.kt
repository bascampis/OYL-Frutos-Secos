package cl.oyl.frutossecos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.oyl.frutossecos.data.local.OperationalPreferences
import cl.oyl.frutossecos.data.local.OperationalPreferencesStore
import cl.oyl.frutossecos.data.local.OperationalPreferencesValidator
import cl.oyl.frutossecos.ui.theme.OylGreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationalPreferencesSheet(
    alias: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val store = remember(context.applicationContext) {
        OperationalPreferencesStore(context.applicationContext)
    }

    val initial = remember(alias) {
        store.read(alias)
    }

    var minimumStock by remember(alias) {
        mutableStateOf(initial.minimumStock.toString())
    }

    var notifyNewOrders by remember(alias) {
        mutableStateOf(initial.notifyNewOrders)
    }

    var notifyLowStock by remember(alias) {
        mutableStateOf(initial.notifyLowStock)
    }

    var minimumStockError by remember(alias) {
        mutableStateOf<String?>(null)
    }

    var saveError by remember(alias) {
        mutableStateOf<String?>(null)
    }

    var saving by remember(alias) {
        mutableStateOf(false)
    }

    var saved by remember(alias) {
        mutableStateOf(false)
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = {
            if (!saving) onDismiss()
        },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Preferencias operativas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Configura las alertas de inventario y pedidos de este administrador.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            OutlinedTextField(
                value = minimumStock,
                onValueChange = {
                    minimumStock = it
                    saved = false
                    saveError = null

                    if (minimumStockError != null) {
                        minimumStockError =
                            OperationalPreferencesValidator
                                .minimumStockError(it)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Stock mínimo para alertas")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null
                    )
                },
                supportingText = {
                    Text(
                        minimumStockError
                            ?: "Un producto tendrá stock bajo cuando tenga esta cantidad o menos."
                    )
                },
                isError = minimumStockError != null,
                enabled = !saving,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = OylGreen
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Pedidos nuevos",
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Mostrar avisos del celular cuando llegue un pedido.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Switch(
                    checked = notifyNewOrders,
                    onCheckedChange = {
                        notifyNewOrders = it
                        saved = false
                        saveError = null
                    },
                    enabled = !saving
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = OylGreen
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Stock bajo",
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Mostrar avisos del celular por existencias bajas.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Switch(
                    checked = notifyLowStock,
                    onCheckedChange = {
                        notifyLowStock = it
                        saved = false
                        saveError = null
                    },
                    enabled = !saving
                )
            }

            Text(
                text = "El permiso de Android y el interruptor general de notificaciones también deben estar activados para recibir avisos del celular.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            saveError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (saved) {
                Text(
                    text = "Preferencias guardadas.",
                    color = OylGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    minimumStockError =
                        OperationalPreferencesValidator
                            .minimumStockError(minimumStock)

                    if (minimumStockError == null) {
                        val values = OperationalPreferences(
                            minimumStock = minimumStock.trim().toInt(),
                            notifyNewOrders = notifyNewOrders,
                            notifyLowStock = notifyLowStock
                        )

                        saving = true
                        saved = false
                        saveError = null

                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    store.save(alias, values)
                                }

                                saved = true
                            } catch (e: CancellationException) {
                                throw e
                            } catch (_: Exception) {
                                saveError =
                                    "No se pudieron guardar los ajustes. Vuelve a intentarlo."
                            } finally {
                                saving = false
                            }
                        }
                    }
                },
                enabled = !saving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OylGreen
                )
            ) {
                Text(
                    text = if (saving) {
                        "Guardando..."
                    } else {
                        "Guardar preferencias"
                    }
                )
            }

            TextButton(
                onClick = onDismiss,
                enabled = !saving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar")
            }
        }
    }
}