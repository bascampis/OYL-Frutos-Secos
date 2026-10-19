package cl.oyl.frutossecos.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.oyl.frutossecos.ui.screens.AboutScreen
import cl.oyl.frutossecos.ui.screens.DashboardScreen
import cl.oyl.frutossecos.ui.screens.LoginScreen
import cl.oyl.frutossecos.ui.screens.WelcomeScreen
import cl.oyl.frutossecos.viewmodel.LoginViewModel
import cl.oyl.frutossecos.viewmodel.SessionViewModel

@Composable
fun OylNavGraph(sessionViewModel: SessionViewModel = viewModel()) {
    val navController = rememberNavController()
    val session by sessionViewModel.user.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = Routes.WELCOME) {
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onBegin = { navController.navigate(Routes.LOGIN) },
                    onAbout = { navController.navigate(Routes.ABOUT) }
                )
            }
            composable(Routes.LOGIN) {
                val loginViewModel: LoginViewModel = viewModel()
                LoginScreen(
                    viewModel = loginViewModel,
                    onBack = { navController.popBackStack() },
                    onAuthenticated = { user ->
                        sessionViewModel.startSession(user)
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Routes.DASHBOARD) {
                if (session == null) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.DASHBOARD) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                } else {
                    DashboardScreen(
                        user = requireNotNull(session),
                        onAbout = { navController.navigate(Routes.ABOUT) },
                        onLogout = {
                            sessionViewModel.endSession()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.DASHBOARD) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
            composable(Routes.ABOUT) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
