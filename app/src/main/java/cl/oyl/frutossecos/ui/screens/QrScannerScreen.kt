package cl.oyl.frutossecos.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.ui.components.OylBottomBar
import cl.oyl.frutossecos.ui.theme.OylGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Cancel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    navController: NavController,
    currentRoute: String,
    onProductFound: (String) -> Unit,
    isAdmin: Boolean = false,
    onAdminInventory: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val app = context.applicationContext as OylApplication
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Apunta la cámara al código QR para obtener información del producto") }
    var isError by remember { mutableStateOf(false) }
    var scanning by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    var failure by remember { mutableStateOf(false) }
    var showPermissionError by remember { mutableStateOf(false) }
    var adminChoiceId by remember { mutableStateOf<String?>(null) }

    val checkScale by animateFloatAsState(
        targetValue = if (success) 1f else 0f,
        animationSpec = tween(350),
        label = "checkScale"
    )
    val crossScale by animateFloatAsState(
        targetValue = if (failure) 1f else 0f,
        animationSpec = tween(350),
        label = "crossScale"
    )

    fun startScan() {
        scanning = true
        isError = false
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
                        if (code.isEmpty()) {
                            status = "El código QR está vacío o no se pudo leer."
                            isError = true
                            failure = true
                            delay(1500)
                            failure = false
                        } else {
                            val product = app.database.catalogDao().getProduct(code)
                            if (product != null) {
                                status = "Producto ${product.id} reconocido"
                                success = true
                                delay(900)
                                success = false
                                if (isAdmin && onAdminInventory != null) adminChoiceId = product.id
                                else onProductFound(product.id)
                            } else {
                                status = "Código $code incorrecto: no corresponde a un producto registrado."
                                isError = true
                                failure = true
                                delay(1500)
                                failure = false
                            }
                        }
                    } catch (_: Exception) {
                        status = "No se pudo consultar el catálogo local."
                        isError = true
                    }
                    scanning = false
                }
            }
            .addOnCanceledListener { status = "Escaneo cancelado."; scanning = false }
            .addOnFailureListener { error ->
                status = "Escáner no disponible: ${error.localizedMessage ?: "Google Play Services requerido"}"
                isError = true
                scanning = false
            }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startScan()
        } else {
            status = "Permiso de cámara denegado."
            isError = true
            showPermissionError = true
        }
    }

    fun onScanClick() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) startScan() else permissionLauncher.launch(Manifest.permission.CAMERA)
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
            // Marco del visor: muestra la cámara o la animación de lectura exitosa
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .border(
                        4.dp,
                        when {
                            success -> OylGreen
                            failure -> Color(0xFFD32F2F)
                            else -> Color.White
                        },
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    success -> Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Lectura exitosa",
                        tint = OylGreen,
                        modifier = Modifier.size(110.dp).scale(checkScale)
                    )
                    failure -> Icon(
                        Icons.Default.Cancel,
                        contentDescription = "Código incorrecto",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(110.dp).scale(crossScale)
                    )
                    else -> Text("📷", fontSize = 48.sp)
                }
            }

            // Botones superiores de flash y galería
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                IconButton(
                    onClick = { onScanClick() },
                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(25.dp)).background(Color.DarkGray)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = "Flash", tint = Color.White)
                }
                IconButton(
                    onClick = { onScanClick() },
                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(25.dp)).background(Color.DarkGray)
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Galería", tint = Color.White)
                }
            }

            // Tarjeta inferior con instrucciones o errores
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
                    Text(
                        status,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Button(
                        onClick = { onScanClick() },
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

    if (showPermissionError) {
        AlertDialog(
            onDismissRequest = { showPermissionError = false },
            title = { Text("Permiso de cámara requerido") },
            text = { Text("Necesitas permitir el acceso a la cámara para escanear códigos QR.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionError = false
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    )
                }) { Text("Abrir ajustes") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionError = false }) { Text("Cancelar") }
            }
        )
    }

    adminChoiceId?.let { id ->
        AlertDialog(
            onDismissRequest = { adminChoiceId = null },
            title = { Text("Producto $id") },
            text = { Text("¿Qué quieres hacer con este producto?") },
            confirmButton = {
                TextButton(onClick = { adminChoiceId = null; onAdminInventory?.invoke(id) }) {
                    Text("Ajustar inventario")
                }
            },
            dismissButton = {
                TextButton(onClick = { adminChoiceId = null; onProductFound(id) }) {
                    Text("Ver ficha")
                }
            }
        )
    }
}