package cl.oyl.frutossecos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.oyl.frutossecos.domain.checkout.formatClp
import cl.oyl.frutossecos.domain.checkout.formatOrderDate
import cl.oyl.frutossecos.viewmodel.OrderConfirmationViewModel

@Composable
fun OrderConfirmationScreen(
    orderId: String,
    ownerAlias: String,
    onViewOrders: () -> Unit,
    onContinueShopping: () -> Unit,
    vm: OrderConfirmationViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    LaunchedEffect(orderId, ownerAlias) { vm.load(ownerAlias, orderId) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp).statusBarsPadding().navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            when {
                state.loading -> CircularProgressIndicator()
                state.order == null -> Text(state.error ?: "Pedido no disponible.", color = MaterialTheme.colorScheme.error)
                else -> {
                    val order = requireNotNull(state.order)
                    AnimatedVisibility(visible, enter = fadeIn() + scaleIn()) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Solicitud recibida",
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
                    }
                    Text("¡Solicitud recibida!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Estado al recibirla: ${order.status}")
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("N.º ${order.id}", style = MaterialTheme.typography.bodySmall)
                            Text("Fecha: ${formatOrderDate(order.createdAt)}")
                            Text("Modalidad: ${order.fulfillment}")
                            Text("Cuenta: ${order.clientAlias}", style = MaterialTheme.typography.bodySmall)
                            HorizontalDivider()
                            order.lines.forEach { line ->
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Image(painterResource(productImage(line.productId)), contentDescription = line.productName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(54.dp).clip(RoundedCornerShape(10.dp)))
                                    Column(Modifier.weight(1f)) {
                                        Text(line.productName, fontWeight = FontWeight.SemiBold)
                                        Text("${line.quantity} × ${formatClp(line.unitPrice.toLong())}")
                                        Text(formatClp(line.quantity.toLong() * line.unitPrice))
                                    }
                                }
                            }
                            HorizontalDivider()
                            Text("Total confirmado: ${formatClp(order.total.toLong())}", fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text("Puedes revisar el estado actualizado en Mis pedidos.", style = MaterialTheme.typography.bodySmall)
                }
            }
            Button(onClick = onViewOrders, modifier = Modifier.fillMaxWidth()) { Text("Ver mis pedidos") }
            OutlinedButton(onClick = onContinueShopping, modifier = Modifier.fillMaxWidth()) { Text("Seguir comprando") }
        }
    }
}
