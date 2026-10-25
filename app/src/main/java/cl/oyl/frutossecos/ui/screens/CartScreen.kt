package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import cl.oyl.frutossecos.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    ownerAlias: String,
    onBack: () -> Unit,
    onCheckout: (String, Int) -> Unit,
    vm: CartViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    LaunchedEffect(ownerAlias) { vm.start(ownerAlias) }
    LaunchedEffect(state.completedOrder, state.busy) {
        if (!state.busy) state.completedOrder?.let { onCheckout(it.id, it.total) }
    }
    val editable = !state.busy && state.completedOrder == null

    Scaffold(topBar = {
        TopAppBar(title = { Text("Mi carrito", fontWeight = FontWeight.Bold) }, navigationIcon = {
            IconButton(onClick = onBack, enabled = editable) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.loading) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (state.rows.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Tu carrito está vacío. Agrega productos desde el catálogo.")
                }
            } else {
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.rows, key = { it.item.productId }) { row ->
                        val product = row.product
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Image(painterResource(productImage(row.item.productId)), contentDescription = row.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)))
                                    Column(Modifier.weight(1f)) {
                                        Text(row.name, fontWeight = FontWeight.Bold)
                                        product?.let {
                                            Text(it.presentation, style = MaterialTheme.typography.bodySmall)
                                            Text("${formatClp(it.price.toLong())} por unidad")
                                        }
                                    }
                                }
                                if (product == null || !product.active || product.stock < row.item.quantity) {
                                    Text(if (product == null || !product.active) "Producto no disponible. Retíralo del carrito."
                                        else "Stock actual: ${product.stock}. Reduce la cantidad.", color = MaterialTheme.colorScheme.error)
                                }
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedIconButton(onClick = { vm.change(row.item.productId, -1) }, enabled = editable) {
                                            Text("−")
                                        }
                                        Text("${row.item.quantity}", Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                                        OutlinedIconButton(onClick = { vm.change(row.item.productId, 1) },
                                            enabled = editable && product != null && product.active && row.item.quantity < product.stock) {
                                            Text("+")
                                        }
                                        IconButton(onClick = { vm.change(row.item.productId, -row.item.quantity) }, enabled = editable) {
                                            Icon(Icons.Default.Delete, contentDescription = "Quitar ${row.name}", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                    Text(formatClp(row.subtotal), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Resumen", fontWeight = FontWeight.Bold)
                    CartSummaryRow("Productos", formatClp(state.subtotal))
                    CartSummaryRow("Retiro", "Sin costo")
                    HorizontalDivider()
                    CartSummaryRow("Total", formatClp(state.subtotal))
                    Text("Modalidad: retiro. El precio y la disponibilidad se revisan antes de confirmar.",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = vm::review, enabled = editable && !state.loading && state.rows.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) {
                if (state.busy) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (state.busy) "Procesando…" else "Revisar pedido")
            }
        }
    }
    if (state.reviewOpen) {
        AlertDialog(onDismissRequest = vm::closeReview,
            title = { Text("Revisar y confirmar pedido") },
            text = {
                Column(Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.rows.forEach { row ->
                        Text("${row.name} × ${row.item.quantity}: ${formatClp(row.subtotal)}")
                    }
                    HorizontalDivider()
                    Text("Retiro sin costo", fontWeight = FontWeight.SemiBold)
                    Text("Total: ${formatClp(state.subtotal)}", fontWeight = FontWeight.Bold)
                    state.contact?.let { contact ->
                        Text("Contacto del perfil: ${contact.name}")
                        Text(contact.email)
                        if (contact.phone.isNotBlank()) Text(contact.phone)
                    }
                    Text("La solicitud quedará pendiente de validación. No se realiza un cobro en esta aplicación.",
                        style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = vm::confirm, enabled = !state.busy) { Text(if (state.busy) "Enviando…" else "Confirmar pedido") }
            },
            dismissButton = { TextButton(onClick = vm::closeReview, enabled = !state.busy) { Text("Volver") } }
        )
    }
}

@Composable
private fun CartSummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
