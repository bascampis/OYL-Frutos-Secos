package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acerca del proyecto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("OYL Frutos Secos", style = MaterialTheme.typography.headlineMedium)
            Text("Versión 0.1.0 · Entrega ZIP 01")
            Text("Aplicación académica para DSY1105 de Duoc UC.")
            Text("Disponible actualmente", style = MaterialTheme.typography.titleLarge)
            Text("Bienvenida animada, selección de rol, inicio de sesión de prueba, panel de rol y cierre de sesión.")
            Text("Todavía no está implementado", style = MaterialTheme.typography.titleLarge)
            Text("Registro de usuarios, Room/SQLite, catálogo, carrito, pedidos, API REST, Spring Boot, cámara, QR, mapa y notificaciones.")
            Text(
                "Las credenciales incluidas son ficticias. Los pedidos y el inventario oficiales requerirán un backend en las siguientes entregas.",
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
