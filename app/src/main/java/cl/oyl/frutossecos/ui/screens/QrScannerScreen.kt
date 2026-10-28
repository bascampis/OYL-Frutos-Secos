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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.ui.components.OylBottomBar
import cl.oyl.frutossecos.ui.theme.OylGreen
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    var status by remember {
        mutableStateOf(
            "Apunta la cámara al código QR de un producto."
        )
    }

    var isError by remember { mutableStateOf(false) }
    var scanning by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    var failure by remember { mutableStateOf(false) }
    var showPermissionError by remember { mutableStateOf(false) }

    var adminChoiceId by remember {
        mutableStateOf<String?>(null)
    }

    var automaticScanStarted by rememberSaveable {
        mutableStateOf(false)
    }

    val checkScale by animateFloatAsState(
        targetValue = if (success) 1f else 0f,
        animationSpec = tween(350),
        label = "Lectura exitosa"
    )

    val crossScale by animateFloatAsState(
        targetValue = if (failure) 1f else 0f,
        animationSpec = tween(350),
        label = "Lectura incorrecta"
    )

    fun startScan() {
        if (scanning || adminChoiceId != null) return

        scanning = true
        isError = false
        success = false
        failure = false
        status = "Apunta la cámara al código QR de un producto."

        try {
            val options = GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .enableAutoZoom()
                .build()

            val scanner = GmsBarcodeScanning.getClient(
                context,
                options
            )

            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    val code = barcode.rawValue
                        ?.trim()
                        .orEmpty()

                    scope.launch {
                        try {
                            app.catalogRepository.seedDemoProducts()

                            val product = if (code.isBlank()) {
                                null
                            } else {
                                app.database.catalogDao()
                                    .productList()
                                    .firstOrNull {
                                        it.id.equals(
                                            code,
                                            ignoreCase = true
                                        )
                                    }
                            }

                            when {
                                code.isBlank() -> {
                                    status = "El código QR está vacío."
                                    isError = true
                                    failure = true

                                    delay(1500)
                                    failure = false
                                }

                                product == null -> {
                                    status =
                                        "El código no corresponde a un producto registrado."

                                    isError = true
                                    failure = true

                                    delay(1500)
                                    failure = false
                                }

                                else -> {
                                    status =
                                        "Producto reconocido: ${product.name}"

                                    success = true

                                    delay(900)
                                    success = false

                                    if (
                                        isAdmin &&
                                        onAdminInventory != null
                                    ) {
                                        adminChoiceId = product.id
                                    } else {
                                        onProductFound(product.id)
                                    }
                                }
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            status =
                                "No se pudo consultar el catálogo local."

                            isError = true
                        } finally {
                            scanning = false
                        }
                    }
                }
                .addOnCanceledListener {
                    status =
                        "Escaneo cancelado. Puedes volver a abrir la cámara."

                    scanning = false
                }
                .addOnFailureListener { error ->
                    status =
                        "Escáner no disponible: ${
                            error.localizedMessage
                                ?: "Comprueba Google Play Services."
                        }"

                    isError = true
                    scanning = false
                }
        } catch (_: Exception) {
            status =
                "No se pudo abrir el escáner. Comprueba Google Play Services."

            isError = true
            scanning = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showPermissionError = false
            startScan()
        } else {
            status = "Permiso de cámara denegado."
            isError = true
            showPermissionError = true
        }
    }

    fun openCamera() {
        if (scanning || adminChoiceId != null) return

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            startScan()
        } else {
            permissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    // Abre la cámara una vez al entrar a esta pantalla.
    LaunchedEffect(Unit) {
        if (!automaticScanStarted) {
            automaticScanStarted = true
            openCamera()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Escanea el código QR del producto",
                        fontWeight = FontWeight.Bold
                    )
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .border(
                        width = 4.dp,
                        color = when {
                            success -> OylGreen
                            failure -> Color(0xFFD32F2F)
                            else -> Color.White
                        },
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    success -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Lectura exitosa",
                            tint = OylGreen,
                            modifier = Modifier
                                .size(110.dp)
                                .scale(checkScale)
                        )
                    }

                    failure -> {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Código incorrecto",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier
                                .size(110.dp)
                                .scale(crossScale)
                        )
                    }

                    scanning -> {
                        CircularProgressIndicator(
                            color = Color.White
                        )
                    }

                    else -> {
                        Text(
                            text = "📷",
                            fontSize = 48.sp
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = status,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    Button(
                        onClick = { openCamera() },
                        enabled = !scanning &&
                                adminChoiceId == null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OylGreen
                        )
                    ) {
                        Text(
                            text = if (scanning) {
                                "Escaneando..."
                            } else {
                                "Volver a escanear"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showPermissionError) {
        AlertDialog(
            onDismissRequest = {
                showPermissionError = false
            },
            title = {
                Text("Permiso de cámara requerido")
            },
            text = {
                Text(
                    "Permite el acceso a la cámara para escanear códigos QR. Después de habilitarlo, vuelve y toca Volver a escanear."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionError = false

                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            ).apply {
                                data = Uri.fromParts(
                                    "package",
                                    context.packageName,
                                    null
                                )
                            }
                        )
                    }
                ) {
                    Text("Abrir ajustes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPermissionError = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    adminChoiceId?.let { id ->
        AlertDialog(
            onDismissRequest = {
                adminChoiceId = null
            },
            title = {
                Text("Producto $id")
            },
            text = {
                Text("¿Qué quieres hacer con este producto?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        adminChoiceId = null
                        onAdminInventory?.invoke(id)
                    }
                ) {
                    Text("Ajustar inventario")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        adminChoiceId = null
                        onProductFound(id)
                    }
                ) {
                    Text("Ver ficha")
                }
            }
        )
    }
}