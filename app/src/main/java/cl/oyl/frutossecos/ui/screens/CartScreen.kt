package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.data.remote.*
import cl.oyl.frutossecos.ui.theme.OylGreen
import cl.oyl.frutossecos.ui.theme.OylMint
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    ownerAlias: String,
    onBack: () -> Unit,
    onCheckout: (String, Int) -> Unit
) {
    var submitting by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = (context.applicationContext as OylApplication).catalogRepository
    val cart by repo.cart(ownerAlias).collectAsState(initial = emptyList())
    val products by repo.products.collectAsState(initial = emptyList())
    val lookup = products.associateBy { it.id }
    val scope = rememberCoroutineScope()

    val subtotal = cart.sumOf { (lookup[it.productId]?.price ?: 0) * it.quantity }
    val despacho = if (cart.isNotEmpty()) 2000 else 0
    val total = subtotal + despacho

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Carrito (${cart.sumOf { it.quantity }})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cart, key = { it.productId }) { line ->
                    val product = lookup[line.productId]
                    if (product != null) {
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
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(OylMint),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🌰", fontSize = 28.sp)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(product.presentation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                    Text("$${product.price}", color = OylGreen, fontWeight = FontWeight.Bold)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    OutlinedIconButton(
                                        onClick = { scope.launch { repo.changeCart(ownerAlias, line.productId, -1) } },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text("−", fontWeight = FontWeight.Bold)
                                    }
                                    Text("${line.quantity}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                    OutlinedIconButton(
                                        onClick = { scope.launch { repo.changeCart(ownerAlias, line.productId, 1) } },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text("+", fontWeight = FontWeight.Bold)
                                    }
                                    IconButton(
                                        onClick = { scope.launch { repo.changeCart(ownerAlias, line.productId, -line.quantity) } },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Resumen", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    SummaryRow("Subtotal", "$$subtotal")
                    SummaryRow("Despacho", "$$despacho")
                    HorizontalDivider()
                    SummaryRow("Total", "$$total", isBold = true)
                }
            }

            if (message.isNotBlank()) {
                Text(message, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    scope.launch {
                        submitting = true
                        try {
                            val order = OylNetwork.api.order(
                                RemoteOrderRequest(
                                    ownerAlias,
                                    "Retiro",
                                    cart.map { RemoteOrderItem(it.productId, it.quantity) }
                                )
                            )
                            repo.clearCart(ownerAlias)
                            repo.syncFromServer()
                            onCheckout(order.id, total)
                        } catch (e: Exception) {
                            message = "No se pudo confirmar: ${e.localizedMessage ?: "servidor desconectado"}"
                        } finally {
                            submitting = false
                        }
                    }
                },
                enabled = cart.isNotEmpty() && !submitting,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OylGreen)
            ) {
                Text(if (submitting) "Validando compra..." else "Continuar compra", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold, color = if (isBold) OylGreen else MaterialTheme.colorScheme.onSurface)
    }
}
