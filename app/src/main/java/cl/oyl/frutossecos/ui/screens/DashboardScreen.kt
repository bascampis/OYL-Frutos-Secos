package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cl.oyl.frutossecos.domain.model.*
import cl.oyl.frutossecos.ui.components.OylBottomBar
import cl.oyl.frutossecos.ui.theme.OylGreen
import cl.oyl.frutossecos.ui.theme.OylMint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: DemoUser,
    navController: NavController,
    currentRoute: String,
    onAbout: () -> Unit,
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    onCatalog: () -> Unit,
    onCart: () -> Unit,
    onQr: () -> Unit,
    onMap: () -> Unit,
    onOrders: () -> Unit,
    onAdmin: () -> Unit,
    onNotifications: () -> Unit
) {
    val admin = user.role == UserRole.ADMINISTRADOR

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("¡Hola, ${user.displayName.substringBefore(" ")}!", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text("Qué bueno verte de nuevo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = onNotifications) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-8).dp, y = 8.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Outlined.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        bottomBar = {
            OylBottomBar(navController = navController, currentRoute = currentRoute, isAdmin = admin)
        }
    ) { p ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(p)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Promo Banner Card
            Card(
                modifier = Modifier.fillMaxWidth().height(110.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = OylGreen)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Compra saludable,", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("vida mejor", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    }
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(OylMint),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🥜", fontSize = 36.sp)
                    }
                }
            }

            // 4 Grid Action Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionGridCard(
                    modifier = Modifier.weight(1f),
                    icon = "📦",
                    title = "Catálogo",
                    onClick = onCatalog
                )
                ActionGridCard(
                    modifier = Modifier.weight(1f),
                    icon = if (admin) "📊" else "📍",
                    title = if (admin) "Inventario" else "Mapa de tiendas",
                    onClick = if (admin) onAdmin else onMap
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionGridCard(
                    modifier = Modifier.weight(1f),
                    icon = "📋",
                    title = "Mis pedidos",
                    onClick = onOrders
                )
                ActionGridCard(
                    modifier = Modifier.weight(1f),
                    icon = "🏷️",
                    title = "Promociones",
                    onClick = onCatalog
                )
            }

            // Featured Products section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Productos destacados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onCatalog) {
                    Text("Ver todos", color = OylGreen, fontWeight = FontWeight.SemiBold)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeaturedProductCard("Mix Premium", "$3.990", "🌰", onCatalog)
                FeaturedProductCard("Almendras", "$2.990", "🥜", onCatalog)
                FeaturedProductCard("Nueces", "$3.490", "🌰", onCatalog)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = onAbout, modifier = Modifier.fillMaxWidth()) {
                Text("Acerca de esta demostración")
            }
        }
    }
}

@Composable
private fun ActionGridCard(modifier: Modifier = Modifier, icon: String, title: String, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(90.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(icon, fontSize = 26.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun FeaturedProductCard(name: String, price: String, icon: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OylMint),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 28.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Text(price, color = OylGreen, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
