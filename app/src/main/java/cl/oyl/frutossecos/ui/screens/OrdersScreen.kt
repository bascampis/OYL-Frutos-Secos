package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cl.oyl.frutossecos.data.remote.*
import cl.oyl.frutossecos.ui.components.OylBottomBar
import cl.oyl.frutossecos.ui.theme.OylGreen
import cl.oyl.frutossecos.ui.theme.OylMint
import kotlinx.coroutines.launch
import cl.oyl.frutossecos.domain.checkout.formatOrderDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    alias: String,
    isAdmin: Boolean,
    navController: NavController,
    currentRoute: String,
    onBack: () -> Unit
) {
    var orders by remember { mutableStateOf<List<RemoteOrder>>(emptyList()) }
    var error by remember { mutableStateOf("") }
    var filterTab by remember { mutableStateOf("Todos") }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(alias, refreshTrigger) {
        try {
            orders = if (isAdmin) OylNetwork.api.allOrders() else OylNetwork.api.orders(alias)
            error = ""
        } catch (_: Exception) {
            error = "No es posible consultar pedidos: servidor desconectado."
        }
    }

    val filteredOrders = orders.filter { order ->
        when (filterTab) {
            "En proceso" -> order.status == "Pendiente de Validación" || order.status == "Confirmado" || order.status == "Preparado"
            "Completados" -> order.status == "Entregado"
            else -> true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isAdmin) "Gestión de Pedidos" else "Mis Pedidos", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { refreshTrigger += 1 }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        },
        bottomBar = {
            OylBottomBar(navController = navController, currentRoute = currentRoute, isAdmin = isAdmin)
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Todos", "En proceso", "Completados").forEach { tab ->
                    FilterChip(
                        selected = filterTab == tab,
                        onClick = { filterTab = tab },
                        label = { Text(tab) }
                    )
                }
            }

            if (error.isNotBlank()) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            if (orders.isEmpty() && error.isBlank()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Sin solicitudes registradas.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredOrders, key = { it.id }) { order ->
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
                                    Text("#${order.id.take(8).uppercase()}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("$${order.total} CLP", fontWeight = FontWeight.ExtraBold, color = OylGreen, style = MaterialTheme.typography.titleMedium)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${formatOrderDate(order.createdAt)} · ${order.fulfillment}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)

                                    // Status Badge Surface
                                    val (badgeBg, badgeText) = when (order.status) {
                                        "Entregado" -> OylMint to OylGreen
                                        "Cancelado" -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
                                        else -> Color(0xFFFFF8E1) to Color(0xFFF57C00)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = badgeBg
                                    ) {
                                        Text(
                                            text = order.status,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            color = badgeText,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }

                                Text(
                                    text = order.lines.joinToString { "${it.productName} × ${it.quantity}" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (isAdmin) {
                                    val next = when (order.status) {
                                        "Pendiente de Validación" -> "Confirmado"
                                        "Confirmado" -> "Preparado"
                                        "Preparado" -> "Entregado"
                                        else -> null
                                    }
                                    if (next != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    try {
                                                        OylNetwork.api.setStatus(order.id, OrderStatusDto(next))
                                                        refreshTrigger += 1
                                                    } catch (_: Exception) {}
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = OylGreen)
                                        ) {
                                            Text("Avanzar estado a $next")
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
