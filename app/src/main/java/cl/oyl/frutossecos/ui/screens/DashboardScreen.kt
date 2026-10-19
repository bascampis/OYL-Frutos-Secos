package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.oyl.frutossecos.domain.model.DemoUser
import cl.oyl.frutossecos.domain.model.UserRole
import cl.oyl.frutossecos.ui.components.FeatureTile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(user: DemoUser, onAbout: () -> Unit, onLogout: () -> Unit) {
    val isAdmin = user.role == UserRole.ADMINISTRADOR
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OYL Frutos Secos") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Outlined.Logout, contentDescription = "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("¡Hola, ${user.displayName}!", style = MaterialTheme.typography.headlineMedium)
            Text(
                if (isAdmin) "Panel de administración · Modo demostración"
                else "Panel de cliente · Modo demostración",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Tu sesión de prueba está activa", style = MaterialTheme.typography.titleMedium)
                    Text("Rol: ${if (isAdmin) "Administrador" else "Cliente"}")
                    Text(
                        "Este inicio de sesión existe solo dentro de la app; no consulta un backend.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Text("Próximos módulos", style = MaterialTheme.typography.titleLarge)
            if (isAdmin) {
                FeatureTile("Gestión de productos", "Alta, edición y activación de artículos.", "Planificado · ZIP 04")
                FeatureTile("Inventario", "Entradas, salidas y avisos de stock bajo.", "Planificado · ZIP 04")
                FeatureTile("Pedidos", "Revisión de solicitudes y sus estados.", "Planificado · ZIP 04")
            } else {
                FeatureTile("Catálogo", "Búsqueda, categorías y fichas de productos.", "Planificado · ZIP 03")
                FeatureTile("Carrito y pedidos", "Carrito individual y solicitudes de compra.", "Planificado · ZIP 03 y 04")
                FeatureTile("Mapa y código QR", "Recursos nativos de ubicación y cámara.", "Planificado · ZIP 05")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onAbout, modifier = Modifier.weight(1f)) { Text("Acerca de") }
                Button(onClick = onLogout, modifier = Modifier.weight(1f)) { Text("Salir") }
            }
        }
    }
}
