package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cl.oyl.frutossecos.data.model.LocalOrder
import cl.oyl.frutossecos.data.model.OrderStatusDto
import cl.oyl.frutossecos.data.repository.LocalStore
import cl.oyl.frutossecos.domain.checkout.formatOrderDate
import cl.oyl.frutossecos.ui.components.OylBottomBar
import cl.oyl.frutossecos.ui.theme.OylGreen
import cl.oyl.frutossecos.ui.theme.OylMint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    alias: String,
    isAdmin: Boolean,
    navController: NavController,
    currentRoute: String,
    onBack: () -> Unit
) {
    var orders by remember(alias, isAdmin) {
        mutableStateOf<List<LocalOrder>>(emptyList())
    }

    var error by remember(alias, isAdmin) {
        mutableStateOf("")
    }

    var message by remember(alias, isAdmin) {
        mutableStateOf("")
    }

    var filterTab by remember(alias, isAdmin) {
        mutableStateOf("Todos")
    }

    var loading by remember(alias, isAdmin) {
        mutableStateOf(false)
    }

    var updatingId by remember(alias, isAdmin) {
        mutableStateOf<String?>(null)
    }

    val scope = rememberCoroutineScope()
    val busy = loading || updatingId != null

    suspend fun refresh() {
        loading = true
        error = ""

        try {
            orders = if (isAdmin) {
                LocalStore.repository.allOrders()
            } else {
                LocalStore.repository.orders(alias)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            error = "No se pudieron consultar los pedidos locales."
        } finally {
            loading = false
        }
    }

    LaunchedEffect(alias, isAdmin) {
        refresh()
    }

    val completedCount = orders.count {
        normalizedOrderStatus(it.status) == "entregado"
    }

    val filteredOrders = orders.filter { order ->
        when (filterTab) {
            "En proceso" -> {
                normalizedOrderStatus(order.status) in setOf(
                    "pendiente de validación",
                    "confirmado",
                    "preparado"
                )
            }

            "Completados" -> {
                normalizedOrderStatus(order.status) == "entregado"
            }

            else -> true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isAdmin) {
                            "Gestión de Pedidos"
                        } else {
                            "Mis Pedidos"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                refresh()
                            }
                        },
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
                isAdmin = isAdmin
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Todos",
                    "En proceso",
                    "Completados"
                ).forEach { tab ->
                    FilterChip(
                        selected = filterTab == tab,
                        onClick = { filterTab = tab },
                        label = { Text(tab) }
                    )
                }
            }

            Text(
                text = "${orders.size} pedidos · $completedCount completados",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            if (loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (error.isNotBlank()) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (message.isNotBlank()) {
                Text(
                    text = message,
                    color = OylGreen,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (filteredOrders.isEmpty() && !loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            error.isNotBlank() ->
                                "Pulsa actualizar para volver a intentarlo."

                            filterTab == "Completados" ->
                                "Todavía no hay pedidos entregados."

                            filterTab == "En proceso" ->
                                "No hay pedidos en proceso."

                            else ->
                                "Sin solicitudes registradas en este dispositivo."
                        }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = filteredOrders,
                        key = { it.id }
                    ) { order ->
                        val status = normalizedOrderStatus(order.status)

                        val badgeColors = when (status) {
                            "entregado" -> OylMint to OylGreen

                            "cancelado" ->
                                Color(0xFFFFEBEE) to Color(0xFFD32F2F)

                            else ->
                                Color(0xFFFFF8E1) to Color(0xFFF57C00)
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#${order.id.take(8).uppercase()}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        text = "$${order.total} CLP",
                                        color = OylGreen,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }

                                Text(
                                    text = "${formatOrderDate(order.createdAt)} · ${order.fulfillment}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                if (isAdmin) {
                                    Text(
                                        text = "Cliente: ${order.clientAlias}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = badgeColors.first
                                ) {
                                    Text(
                                        text = order.status,
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 4.dp
                                        ),
                                        color = badgeColors.second,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }

                                Text(
                                    text = order.lines.joinToString {
                                        "${it.productName} × ${it.quantity}"
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                if (isAdmin) {
                                    val next = when (status) {
                                        "pendiente de validación" -> "Confirmado"
                                        "confirmado" -> "Preparado"
                                        "preparado" -> "Entregado"
                                        else -> null
                                    }

                                    if (next != null) {
                                        Button(
                                            onClick = {
                                                if (!busy) {
                                                    updatingId = order.id
                                                    error = ""
                                                    message = ""

                                                    scope.launch {
                                                        try {
                                                            val updated =
                                                                LocalStore.repository.setStatus(
                                                                    order.id,
                                                                    OrderStatusDto(next)
                                                                )

                                                            orders = orders.map {
                                                                if (it.id == updated.id) {
                                                                    updated
                                                                } else {
                                                                    it
                                                                }
                                                            }

                                                            message =
                                                                "Pedido #${updated.id.take(8).uppercase()}: ${updated.status}."
                                                        } catch (e: CancellationException) {
                                                            throw e
                                                        } catch (_: Exception) {
                                                            error =
                                                                "No se pudo cambiar el estado. Actualiza la lista y vuelve a intentarlo."
                                                        } finally {
                                                            updatingId = null
                                                        }
                                                    }
                                                }
                                            },
                                            enabled = !busy,
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = OylGreen
                                            )
                                        ) {
                                            Text(
                                                if (updatingId == order.id) {
                                                    "Guardando..."
                                                } else {
                                                    "Avanzar estado a $next"
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun normalizedOrderStatus(status: String): String {
    return status.trim().lowercase(Locale.ROOT)
}