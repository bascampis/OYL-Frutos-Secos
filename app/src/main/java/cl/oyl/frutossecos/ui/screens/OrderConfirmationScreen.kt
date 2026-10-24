package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.oyl.frutossecos.ui.theme.OylGreen
import cl.oyl.frutossecos.ui.theme.OylMint

@Composable
fun OrderConfirmationScreen(
    orderId: String,
    totalAmount: Int,
    onViewOrders: () -> Unit,
    onContinueShopping: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Box with box icon and checkmark badge
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(OylMint, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📦", fontSize = 54.sp)
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(OylGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "¡Pedido confirmado!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = OylGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Tu pedido ha sido recibido correctamente y está en proceso de validación.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OrderInfoRow("N° de pedido", "#${orderId.take(8).uppercase()}")
                    HorizontalDivider()
                    OrderInfoRow("Fecha", "12/09/2026 - 14:30")
                    HorizontalDivider()
                    OrderInfoRow("Total", "$$totalAmount CLP")
                    HorizontalDivider()
                    OrderInfoRow("Método de entrega", "Retiro en tienda OYL")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onViewOrders,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OylGreen)
            ) {
                Text("Ver mis pedidos", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onContinueShopping,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Seguir comprando", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun OrderInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.secondary)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
