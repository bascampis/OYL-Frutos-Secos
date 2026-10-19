package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import cl.oyl.frutossecos.OylApplication
import kotlinx.coroutines.launch

/**
 * Escaneo REAL a través de Google Code Scanner / Play Services.
 * Requiere dispositivo compatible y Google Play Services; no es simulación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(onBack: () -> Unit) {
 val context = LocalContext.current
 val app = context.applicationContext as OylApplication
 val scope = rememberCoroutineScope()
 var status by remember { mutableStateOf("Escanea un QR con identificador de producto, por ejemplo OY001.") }
 var scanning by remember { mutableStateOf(false) }
 Scaffold(topBar = { TopAppBar(title = { Text("Escáner QR de productos") }) }) { padding ->
  Column(Modifier.fillMaxSize().padding(padding).padding(20.dp),
   verticalArrangement = Arrangement.spacedBy(16.dp)) {
   Text("Escáner de cámara", style = MaterialTheme.typography.headlineSmall)
   Text("La lectura utiliza Google Code Scanner. No necesita permiso de cámara de la aplicación.")
   Text(status)
   Button(onClick = {
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
        status = if (product != null)
         "Lectura correcta: " + product.name + " · $" + product.price + " CLP (datos DEMO)"
        else "Código leído: " + code + ". No corresponde a un producto local."
       } catch (_: Exception) { status = "No se pudo consultar el catálogo local." }
       scanning = false
      }
     }
     .addOnCanceledListener { status = "Escaneo cancelado."; scanning = false }
     .addOnFailureListener { error ->
      status = "Escáner no disponible: " + (error.localizedMessage ?: "revisa Google Play Services")
      scanning = false
     }
   }, enabled = !scanning, modifier = Modifier.fillMaxWidth()) {
    Text(if (scanning) "Abriendo cámara..." else "Escanear QR")
   }
   OutlinedButton(onBack, Modifier.fillMaxWidth()) { Text("Volver") }
  }
 }
}