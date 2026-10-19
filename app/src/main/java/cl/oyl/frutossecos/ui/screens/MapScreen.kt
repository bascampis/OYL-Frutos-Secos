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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/**
 * Ubicación del usuario: recurso nativo real. Las tiendas son referenciales y no
 * incluyen coordenadas inventadas: la tienda real debe ser configurada después.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onBack: () -> Unit) {
 val context = LocalContext.current
 var info by remember { mutableStateOf("Puedes consultar tu posición actual o abrir un mapa externo.") }
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
       "Tu posición aproximada: " + location.latitude + ", " + location.longitude
      else "Ubicación no disponible. Activa ubicación del dispositivo."
    }
   } else {
    @Suppress("DEPRECATION")
    val last = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    info = if (last == null) "Ubicación no disponible." else
     "Última posición aproximada: " + last.latitude + ", " + last.longitude
   }
  } catch (e: Exception) { info = "No se pudo obtener ubicación: " + (e.localizedMessage ?: "error") }
 }
 val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
  if (granted) locate() else info = "Permiso denegado. Puedes utilizar el mapa sin compartir tu ubicación."
 }
 Scaffold(topBar = { TopAppBar(title = { Text("Mapa y ubicación") }) }) { padding ->
  Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
   Text("Mi ubicación", style = MaterialTheme.typography.headlineSmall)
   Text(info)
   Button(onClick = {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
     locate()
    else permission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
   }, modifier = Modifier.fillMaxWidth()) { Text("Consultar ubicación nativa") }
   OutlinedButton(onClick = {
    try {
     val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=frutos+secos+Santiago+Chile"))
     context.startActivity(intent)
    } catch (_: Exception) { info = "No hay aplicación de mapas instalada." }
   }, modifier = Modifier.fillMaxWidth()) { Text("Buscar frutos secos en mapa externo") }
   Text("Puntos mostrados por aplicaciones externas no son puntos de retiro confirmados de OYL.")
   OutlinedButton(onBack, Modifier.fillMaxWidth()) { Text("Volver") }
  }
 }
}