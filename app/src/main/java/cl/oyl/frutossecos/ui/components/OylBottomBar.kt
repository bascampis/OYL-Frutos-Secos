package cl.oyl.frutossecos.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import cl.oyl.frutossecos.navigation.Routes

sealed class BottomNavTab(val route: String, val title: String, val icon: ImageVector) {
    object Inicio : BottomNavTab(Routes.DASHBOARD, "Inicio", Icons.Default.Home)
    object Mapa : BottomNavTab(Routes.MAP, "Mapa", Icons.Default.Place)
    object Qr : BottomNavTab(Routes.QR, "Escáner", Icons.Default.QrCodeScanner)
    object Pedidos : BottomNavTab(Routes.ORDERS, "Pedidos", Icons.AutoMirrored.Filled.ListAlt)
    object Inventario : BottomNavTab(Routes.INVENTORY_MANAGEMENT, "Inventario", Icons.Default.Inventory)
    object Productos : BottomNavTab(Routes.PRODUCT_MANAGEMENT, "Productos", Icons.Default.Store)
    object Perfil : BottomNavTab(Routes.PROFILE, "Perfil", Icons.Default.Person)
}

@Composable
fun OylBottomBar(navController: NavController, currentRoute: String, isAdmin: Boolean) {
    val tabs = if (isAdmin) {
        listOf(
            BottomNavTab.Inicio,
            BottomNavTab.Inventario,
            BottomNavTab.Qr,
            BottomNavTab.Pedidos,
            BottomNavTab.Perfil
        )
    } else {
        listOf(
            BottomNavTab.Inicio,
            BottomNavTab.Mapa,
            BottomNavTab.Qr,
            BottomNavTab.Pedidos,
            BottomNavTab.Perfil
        )
    }

    NavigationBar {
        tabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (selected) return@NavigationBarItem
                    if (tab.route == Routes.DASHBOARD) {
                        // Inicio: sacar de la pila todo lo que esté encima del dashboard.
                        val volvio = navController.popBackStack(Routes.DASHBOARD, inclusive = false)
                        if (!volvio) {
                            navController.navigate(Routes.DASHBOARD) { launchSingleTop = true }
                        }
                    } else {
                        navController.navigate(tab.route) {
                            popUpTo(Routes.DASHBOARD)
                            launchSingleTop = true
                        }
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.title) },
                label = { Text(tab.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}