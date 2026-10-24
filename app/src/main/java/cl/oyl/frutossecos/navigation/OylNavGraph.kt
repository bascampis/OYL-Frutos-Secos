package cl.oyl.frutossecos.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import cl.oyl.frutossecos.domain.model.DemoUser
import cl.oyl.frutossecos.ui.screens.*
import cl.oyl.frutossecos.viewmodel.*

@Composable
fun OylNavGraph(sessionViewModel: SessionViewModel = viewModel()) {
    val nav = rememberNavController()
    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.WELCOME

    val user by sessionViewModel.user.collectAsState()

    fun loggedIn(value: DemoUser) {
        sessionViewModel.startSession(value)
        nav.navigate(Routes.DASHBOARD) {
            popUpTo(Routes.WELCOME) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun logOut() {
        sessionViewModel.endSession()
        nav.navigate(Routes.LOGIN) {
            popUpTo(Routes.DASHBOARD) { inclusive = true }
            launchSingleTop = true
        }
    }

    Surface(Modifier.fillMaxSize()) {
        NavHost(nav, startDestination = if (user == null) Routes.WELCOME else Routes.DASHBOARD) {
            // 1. Welcome Screen
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onBegin = { nav.navigate(Routes.LOGIN) },
                    onLogin = { nav.navigate(Routes.LOGIN) },
                    onAbout = { nav.navigate(Routes.ABOUT) }
                )
            }

            // 2. Register Screen
            composable(Routes.REGISTER) {
                val vm: RegisterViewModel = viewModel()
                RegisterScreen(
                    vm,
                    onBack = { nav.popBackStack() },
                    onDone = ::loggedIn
                )
            }

            // 3. Login Screen
            composable(Routes.LOGIN) {
                val vm: LoginViewModel = viewModel()
                LoginScreen(
                    vm,
                    onBack = { nav.popBackStack() },
                    onAuthenticated = ::loggedIn,
                    onRegister = { nav.navigate(Routes.REGISTER) }
                )
            }

            // 4. Dashboard Screen
            composable(Routes.DASHBOARD) {
                val active = user
                if (active != null) {
                    DashboardScreen(
                        user = active,
                        navController = nav,
                        currentRoute = currentRoute,
                        onAbout = { nav.navigate(Routes.ABOUT) },
                        onLogout = ::logOut,
                        onProfile = { nav.navigate(Routes.PROFILE) },
                        onCatalog = { nav.navigate(Routes.CATALOG) },
                        onCart = { nav.navigate(Routes.CART) },
                        onQr = { nav.navigate(Routes.QR) },
                        onMap = { nav.navigate(Routes.MAP) },
                        onOrders = { nav.navigate(Routes.ORDERS) },
                        onAdmin = { nav.navigate(Routes.PRODUCT_MANAGEMENT) },
                        onNotifications = { nav.navigate(Routes.NOTIFICATIONS) }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        nav.navigate(Routes.LOGIN) { popUpTo(Routes.DASHBOARD) { inclusive = true } }
                    }
                }
            }

            // 5. Map Screen
            composable(Routes.MAP) {
                val active = user
                if (active != null) {
                    MapScreen(
                        navController = nav,
                        currentRoute = currentRoute,
                        onBack = { nav.popBackStack() }
                    )
                } else {
                    LaunchedEffect(Unit) { nav.navigate(Routes.LOGIN) }
                }
            }

            // 6. Catalog Screen
            composable(Routes.CATALOG) {
                val active = user
                if (active != null) {
                    CatalogScreen(
                        ownerAlias = active.alias,
                        isAdmin = active.role == cl.oyl.frutossecos.domain.model.UserRole.ADMINISTRADOR,
                        navController = nav,
                        currentRoute = currentRoute,
                        onBack = { nav.popBackStack() },
                        onCart = { nav.navigate(Routes.CART) },
                        onProductClick = { productId -> nav.navigate(Routes.productDetail(productId)) }
                    )
                }
            }

            // 7. Product Detail Screen
            composable(
                route = Routes.PRODUCT_DETAIL,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val active = user
                val productId = backStackEntry.arguments?.getString("productId") ?: "OY001"
                if (active != null) {
                    ProductDetailScreen(
                        productId = productId,
                        ownerAlias = active.alias,
                        onBack = { nav.popBackStack() },
                        onCart = { nav.navigate(Routes.CART) }
                    )
                }
            }

            // 8. QR Scanner Screen
            composable(Routes.QR) {
                val active = user
                if (active != null) {
                    QrScannerScreen(
                        navController = nav,
                        currentRoute = currentRoute,
                        onProductFound = { productId -> nav.navigate(Routes.productDetail(productId)) }
                    )
                }
            }

            // 9. Cart Screen
            composable(Routes.CART) {
                val active = user
                if (active != null && active.role == cl.oyl.frutossecos.domain.model.UserRole.CLIENTE) {
                    CartScreen(
                        ownerAlias = active.alias,
                        onBack = { nav.popBackStack() },
                        onCheckout = { orderId, total ->
                            nav.navigate(Routes.orderConfirmation(orderId) + "?total=$total") {
                                popUpTo(Routes.CART) { inclusive = true }
                            }
                        }
                    )
                }
            }

            // 10. Order Confirmation Screen
            composable(
                route = Routes.ORDER_CONFIRMATION + "?total={total}",
                arguments = listOf(
                    navArgument("orderId") { type = NavType.StringType },
                    navArgument("total") { type = NavType.IntType; defaultValue = 12470 }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: "001245"
                val total = backStackEntry.arguments?.getInt("total") ?: 12470
                OrderConfirmationScreen(
                    orderId = orderId,
                    totalAmount = total,
                    onViewOrders = {
                        nav.navigate(Routes.ORDERS) {
                            popUpTo(Routes.DASHBOARD)
                        }
                    },
                    onContinueShopping = {
                        nav.navigate(Routes.CATALOG) {
                            popUpTo(Routes.DASHBOARD)
                        }
                    }
                )
            }

            // 11. Product Management (Admin)
            composable(Routes.PRODUCT_MANAGEMENT) {
                val active = user
                if (active?.role == cl.oyl.frutossecos.domain.model.UserRole.ADMINISTRADOR) {
                    ProductManagementScreen(
                        navController = nav,
                        currentRoute = currentRoute
                    )
                }
            }

            // 12. Inventory Management (Admin)
            composable(Routes.INVENTORY_MANAGEMENT) {
                val active = user
                if (active?.role == cl.oyl.frutossecos.domain.model.UserRole.ADMINISTRADOR) {
                    InventoryScreen(
                        navController = nav,
                        currentRoute = currentRoute
                    )
                }
            }

            // 13. Orders History Screen
            composable(Routes.ORDERS) {
                val active = user
                if (active != null) {
                    OrdersScreen(
                        alias = active.alias,
                        isAdmin = active.role == cl.oyl.frutossecos.domain.model.UserRole.ADMINISTRADOR,
                        navController = nav,
                        currentRoute = currentRoute,
                        onBack = { nav.popBackStack() }
                    )
                }
            }

            // 14. Notifications Screen
            composable(Routes.NOTIFICATIONS) {
                val active = user
                if (active != null) {
                    NotificationsScreen(
                        alias = active.alias,
                        onBack = { nav.popBackStack() }
                    )
                }
            }

            // 15. Profile Screen
            composable(Routes.PROFILE) {
                val active = user
                if (active != null) {
                    ProfileScreen(
                        user = active,
                        navController = nav,
                        currentRoute = currentRoute,
                        onBack = { nav.popBackStack() },
                        onLogout = ::logOut
                    )
                } else {
                    LaunchedEffect(Unit) { nav.navigate(Routes.LOGIN) }
                }
            }

            // About Screen
            composable(Routes.ABOUT) {
                AboutScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}
