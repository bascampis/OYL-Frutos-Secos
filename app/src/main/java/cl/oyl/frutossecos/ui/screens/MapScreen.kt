package cl.oyl.frutossecos.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import cl.oyl.frutossecos.ui.components.OylBottomBar
import cl.oyl.frutossecos.ui.theme.OylGreen
import cl.oyl.frutossecos.ui.theme.OylMint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    currentRoute: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var info by remember { mutableStateOf("Tienda OYL - Santiago Centro · Abierta 8:00 - 19:00") }
    var searchQuery by remember { mutableStateOf("") }
    var filterTab by remember { mutableStateOf("Todas") }

    fun locate() {
        try {
            val manager = context.getSystemService(LocationManager::class.java)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                info = "Falta permiso de ubicación."; return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                manager.getCurrentLocation(LocationManager.NETWORK_PROVIDER, CancellationSignal(),
                    ContextCompat.getMainExecutor(context)) { location ->
                    info = if (location != null)
                        "Tu posición: ${location.latitude}, ${location.longitude}"
                    else "Ubicación no disponible."
                }
            } else {
                @Suppress("DEPRECATION")
                val last = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                info = if (last == null) "Ubicación no disponible." else "Última posición: ${last.latitude}, ${last.longitude}"
            }
        } catch (e: Exception) { info = "Error al obtener ubicación: ${e.localizedMessage}" }
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) locate() else info = "Permiso denegado."
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mapa de Tiendas y Puntos", fontWeight = FontWeight.Bold) })
        },
        bottomBar = {
            OylBottomBar(navController = navController, currentRoute = currentRoute, isAdmin = false)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Simulated Map background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OylMint),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🗺️", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Mapa interactivo de Tiendas y Puntos OYL", fontWeight = FontWeight.Bold, color = OylGreen)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(info, style = MaterialTheme.typography.bodySmall)
                }
            }

            // Top overlay search and filters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar tiendas o puntos de retiro...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Todas", "Tiendas", "Puntos de retiro").forEach { tab ->
                        FilterChip(
                            selected = filterTab == tab,
                            onClick = { filterTab = tab },
                            label = { Text(tab) }
                        )
                    }
                }
            }

            // Bottom store card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Tienda OYL - Santiago Centro", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Av. Libertador B. O'Higgins 1234, Santiago, RM", style = MaterialTheme.typography.bodyMedium)
                    Text("Abierta • 8:00 - 19:00", color = OylGreen, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=Av.+Libertador+B.+O'Higgins+1234+Santiago"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = OylGreen)
                        ) {
                            Text("Cómo llegar")
                        }

                        OutlinedButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                                    locate()
                                else permission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Mi ubicación")
                        }
                    }
                }
            }
        }
    }
}
