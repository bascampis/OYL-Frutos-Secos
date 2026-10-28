package cl.oyl.frutossecos.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import cl.oyl.frutossecos.OylApplication
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

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

    var adminChoiceId by remember {
        mutableStateOf<String?>(null)
    }

    var finished by remember {
        mutableStateOf(false)
    }

    fun closeScanner(message: String? = null) {
        if (finished) return
        finished = true

        if (message != null) {
            Toast.makeText(
                context,
                message,
                Toast.LENGTH_LONG
            ).show()
        }

        navController.popBackStack()
    }

    fun openProduct(productId: String) {
        if (finished) return
        finished = true

        navController.popBackStack()
        onProductFound(productId)
    }

    fun openInventory(productId: String) {
        if (finished) return
        finished = true

        navController.popBackStack()
        onAdminInventory?.invoke(productId)
    }

    LaunchedEffect(Unit) {
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
                            if (code.isBlank()) {
                                closeScanner(
                                    "El código QR está vacío."
                                )
                                return@launch
                            }

                            app.catalogRepository.seedDemoProducts()

                            val product = app.database
                                .catalogDao()
                                .productList()
                                .firstOrNull {
                                    it.id.equals(
                                        code,
                                        ignoreCase = true
                                    )
                                }

                            if (product == null) {
                                closeScanner(
                                    "El QR no corresponde a un producto registrado."
                                )
                                return@launch
                            }

                            if (
                                isAdmin &&
                                onAdminInventory != null
                            ) {
                                adminChoiceId = product.id
                            } else {
                                openProduct(product.id)
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            closeScanner(
                                "No se pudo consultar el producto."
                            )
                        }
                    }
                }
                .addOnCanceledListener {
                    closeScanner()
                }
                .addOnFailureListener { error ->
                    closeScanner(
                        "No se pudo abrir el lector: ${
                            error.localizedMessage
                                ?: "Comprueba Google Play Services."
                        }"
                    )
                }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            closeScanner(
                "No se pudo iniciar el lector QR."
            )
        }
    }

    // Pantalla transitoria mientras Google abre la cámara.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (adminChoiceId == null) {
                CircularProgressIndicator()
            }
        }
    }

    adminChoiceId?.let { productId ->
        AlertDialog(
            onDismissRequest = {
                closeScanner()
            },
            title = {
                Text("Producto $productId")
            },
            text = {
                Text("¿Qué quieres hacer con este producto?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openInventory(productId)
                    }
                ) {
                    Text("Ajustar inventario")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openProduct(productId)
                    }
                ) {
                    Text("Ver producto")
                }
            }
        )
    }
}