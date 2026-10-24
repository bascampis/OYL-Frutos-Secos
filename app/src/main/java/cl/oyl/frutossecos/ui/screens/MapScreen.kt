package cl.oyl.frutossecos.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cl.oyl.frutossecos.data.local.MapPointEntity
import cl.oyl.frutossecos.ui.components.OylBottomBar
import cl.oyl.frutossecos.viewmodel.MapViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    currentRoute: String,
    onBack: () -> Unit,
    vm: MapViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    var message by remember { mutableStateOf<String?>(null) }
    var showDetails by remember { mutableStateOf(false) }
    var userPosition by remember { mutableStateOf<GeoPoint?>(null) }
    var locating by remember { mutableStateOf(false) }
    val manager = remember { context.getSystemService(LocationManager::class.java) }
    val handler = remember { Handler(Looper.getMainLooper()) }
    var cancelLocation by remember { mutableStateOf<(() -> Unit)?>(null) }

    val map = remember(context) {
        Configuration.getInstance().apply {
            userAgentValue = "${context.packageName}/OYL-Academic-Map"
            osmdroidBasePath = File(context.filesDir, "oyl_map").apply { mkdirs() }
            osmdroidTileCache = File(osmdroidBasePath, "tiles").apply { mkdirs() }
        }
        MapView(context).apply {
            setTileSource(XYTileSource("OYL-OSM", 0, 19, 256, ".png",
                arrayOf("https://tile.openstreetmap.org/")))
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
            controller.setZoom(13.5)
            controller.setCenter(GeoPoint(-33.442, -70.647))
        }
    }

    DisposableEffect(map, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> map.onResume()
                Lifecycle.Event.ON_PAUSE -> { cancelLocation?.invoke(); map.onPause() }
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) map.onResume()
        onDispose {
            cancelLocation?.invoke()
            lifecycle.removeObserver(observer)
            map.onPause()
            map.onDetach()
        }
    }

    fun locate() {
        if (locating) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            message = "Falta permiso de ubicación aproximada."
            return
        }
        try {
            val provider = LocationManager.NETWORK_PROVIDER
            if (!manager.isProviderEnabled(provider)) {
                message = "Activa la ubicación y la conexión de red del dispositivo."
                return
            }
            locating = true
            message = "Buscando tu ubicación aproximada…"
            val signal = CancellationSignal()
            var listener: LocationListener? = null
            var finished = false
            var timeout: Runnable? = null
            fun finish(location: Location?, error: String? = null) {
                if (finished) return
                finished = true
                signal.cancel()
                listener?.let { manager.removeUpdates(it) }
                timeout?.let { handler.removeCallbacks(it) }
                locating = false
                cancelLocation = null
                if (location != null) {
                    userPosition = GeoPoint(location.latitude, location.longitude)
                    map.controller.setZoom(15.0)
                    map.controller.animateTo(userPosition)
                    message = "Ubicación aproximada encontrada (marcador azul)."
                } else message = error ?: "No se pudo obtener tu ubicación. Inténtalo nuevamente."
            }
            cancelLocation = { finish(null, "Búsqueda de ubicación cancelada.") }
            timeout = Runnable { finish(null, "No se obtuvo ubicación en 15 segundos. Revisa la ubicación del dispositivo.") }
            handler.postDelayed(timeout!!, 15_000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                manager.getCurrentLocation(provider, signal, ContextCompat.getMainExecutor(context)) {
                    finish(it)
                }
            } else {
                listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) { finish(location) }
                    override fun onProviderDisabled(provider: String) { finish(null, "Ubicación desactivada.") }
                    override fun onProviderEnabled(provider: String) = Unit
                    @Deprecated("Legacy Android callback")
                    override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) = Unit
                }
                @Suppress("DEPRECATION")
                manager.requestSingleUpdate(provider, listener!!, Looper.getMainLooper())
            }
        } catch (_: Exception) {
            cancelLocation?.invoke()
            locating = false
            message = "No se pudo obtener ubicación. Comprueba el permiso y los servicios de ubicación."
        }
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) locate() else message = "Permiso denegado. Puedes seguir consultando los puntos del mapa."
    }
    val visible = state.visiblePoints
    val selected = state.selected

    LaunchedEffect(visible, userPosition, map) {
        map.overlays.removeAll { it is Marker }
        visible.forEach { point ->
            val marker = Marker(map).apply {
                position = GeoPoint(point.latitude, point.longitude)
                title = point.name
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = mapPin(context, if (point.type == "Tienda") 0xFF246B48.toInt() else 0xFF3465B4.toInt(),
                    if (point.type == "Tienda") "T" else "R")
                setOnMarkerClickListener { _, _ ->
                    vm.select(point.id)
                    map.controller.animateTo(position)
                    true
                }
            }
            map.overlays.add(marker)
        }
        userPosition?.let { position ->
            map.overlays.add(Marker(map).apply {
                this.position = position
                title = "Tu ubicación aproximada"
                icon = mapPin(context, 0xFF167ED3.toInt(), "●")
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            })
        }
        map.invalidate()
    }
    LaunchedEffect(selected?.id) {
        selected?.let { map.controller.animateTo(GeoPoint(it.latitude, it.longitude)) }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Tiendas y puntos de retiro", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                })
        },
        bottomBar = { OylBottomBar(navController, currentRoute, isAdmin = false) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = state.query, onValueChange = vm::search,
                    placeholder = { Text("Buscar tiendas o puntos de retiro…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()
                )
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Todas", "Tiendas", "Puntos de retiro").forEach { filter ->
                        FilterChip(selected = state.filter == filter, onClick = { vm.filter(filter) }, label = { Text(filter) })
                    }
                }
                Text("Puntos y horarios ficticios para demostración", style = MaterialTheme.typography.labelSmall)
            }
            Box(Modifier.fillMaxWidth().weight(1f)) {
                AndroidView(factory = { map }, modifier = Modifier.fillMaxSize())
                FilledTonalIconButton(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) locate()
                        else permission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }, enabled = !locating, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    if (locating) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
                }
                Surface(Modifier.align(Alignment.BottomEnd), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)) {
                    Text("© OpenStreetMap contributors", style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.clickable { uriHandler.openUri("https://www.openstreetmap.org/copyright") }.padding(4.dp))
                }
                if (state.loading) CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
            if (message != null || state.error != null) {
                Text(state.error ?: message.orEmpty(), modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall)
            }
            if (!state.loading && selected == null) {
                Text("No hay puntos para esta búsqueda.", modifier = Modifier.padding(16.dp))
            }
            selected?.let { point ->
                Card(Modifier.fillMaxWidth().padding(12.dp), shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                    Column(Modifier.heightIn(max = 225.dp).verticalScroll(rememberScrollState()).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(point.name, fontWeight = FontWeight.Bold)
                        Text(point.address, style = MaterialTheme.typography.bodySmall)
                        Text(point.hours, style = MaterialTheme.typography.bodySmall)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                try {
                                    // Coordenadas idénticas al marcador. El navegador ofrece alternativa si no hay app de mapas.
                                    val url = "https://www.google.com/maps/dir/?api=1&destination=${point.latitude},${point.longitude}"
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                } catch (_: Exception) { message = "No hay una aplicación disponible para abrir indicaciones." }
                            }, modifier = Modifier.weight(1f)) { Text("Cómo llegar") }
                            OutlinedButton(onClick = { showDetails = true }, modifier = Modifier.weight(1f)) { Text("Ver detalles") }
                        }
                    }
                }
            }
        }
    }
    if (showDetails && selected != null) {
        AlertDialog(onDismissRequest = { showDetails = false }, title = { Text(selected.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (selected.type == "Tienda") "Tipo: tienda" else "Tipo: punto de retiro")
                    Text(selected.address)
                    Text(selected.hours)
                    Text(selected.instructions)
                    Text("Coordenadas: ${selected.latitude}, ${selected.longitude}")
                }
            }, confirmButton = { TextButton(onClick = { showDetails = false }) { Text("Cerrar") } })
    }
}

private fun mapPin(context: android.content.Context, color: Int, letter: String): BitmapDrawable {
    val bitmap = Bitmap.createBitmap(72, 96, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
    val path = Path().apply { moveTo(12f, 52f); lineTo(36f, 94f); lineTo(60f, 52f); close() }
    canvas.drawPath(path, paint)
    canvas.drawCircle(36f, 35f, 30f, paint)
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 30f
    paint.textAlign = Paint.Align.CENTER
    paint.isFakeBoldText = true
    canvas.drawText(letter, 36f, 46f, paint)
    return BitmapDrawable(context.resources, bitmap)
}
