package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.ui.components.OylBottomBar
import cl.oyl.frutossecos.ui.theme.OylGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    navController: NavController,
    currentRoute: String,
    onProductFound: (String) -> Unit,
    isAdmin: Boolean = false
) {
    val context = LocalContext.current
    val app = context.applicationContext as OylApplication
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Apunta la cámara al código QR para obtener información del producto") }
    var scanning by remember { mutableStateOf(false) }

    fun startScan() {
        scanning = true
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()
        val scanner = GmsBarcodeScanning.getClient(context, options)
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val code = barcode.rawValue?.trim()?.uppercase().orEmpty()
                scope.launch {
                    try {
                        val product = app.database.catalogDao().getProduct(code)
                        if (product != null) {
                            onProductFound(product.id)
                        } else {
                            status = "Código $code leído. No corresponde a un producto registrado."
                        }
                    } catch (_: Exception) {
                        status = "No se pudo consultar el catálogo local."
                    }
                    scanning = false
                }
            }
            .addOnCanceledListener { status = "Escaneo cancelado."; scanning = false }
            .addOnFailureListener { error ->
                status = "Escáner no disponible: ${error.localizedMessage ?: "Google Play Services requerido"}"
                scanning = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Escanea el código QR del producto", fontWeight = FontWeight.Bold) })
        },
        bottomBar = {
            OylBottomBar(navController = navController, currentRoute = currentRoute, isAdmin = isAdmin)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Viewfinder box overlay
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .border(4.dp, Color.White, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("📷", fontSize = 48.sp)
            }

            // Top flash & gallery buttons
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                IconButton(
                    onClick = { startScan() },
                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(25.dp)).background(Color.DarkGray)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = "Flash", tint = Color.White)
                }
                IconButton(
                    onClick = { startScan() },
                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(25.dp)).background(Color.DarkGray)
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Galería", tint = Color.White)
                }
            }

            // Bottom instruction card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(status, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                    Button(
                        onClick = { startScan() },
                        enabled = !scanning,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OylGreen)
                    ) {
                        Text(if (scanning) "Escaneando..." else "Escanear QR ahora", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
