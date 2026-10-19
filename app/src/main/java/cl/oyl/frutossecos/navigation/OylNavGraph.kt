package cl.oyl.frutossecos.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import cl.oyl.frutossecos.domain.model.DemoUser
import cl.oyl.frutossecos.ui.screens.*
import cl.oyl.frutossecos.viewmodel.*

@Composable fun OylNavGraph(sessionViewModel: SessionViewModel = viewModel()) {
 val nav = rememberNavController()
 val user by sessionViewModel.user.collectAsState()
 fun loggedIn(value: DemoUser) {
  sessionViewModel.startSession(value)
  nav.navigate(Routes.DASHBOARD) { popUpTo(Routes.WELCOME) { inclusive = true }; launchSingleTop = true }
 }
 fun logOut() {
  sessionViewModel.endSession()
  nav.navigate(Routes.LOGIN) { popUpTo(Routes.DASHBOARD) { inclusive = true }; launchSingleTop = true }
 }
 Surface(Modifier.fillMaxSize()) {
  NavHost(nav, startDestination = if (user == null) Routes.WELCOME else Routes.DASHBOARD) {
   composable(Routes.WELCOME) {
    WelcomeScreen(onBegin = { nav.navigate(Routes.LOGIN) }, onAbout = { nav.navigate(Routes.ABOUT) })
   }
   composable(Routes.LOGIN) {
    val vm: LoginViewModel = viewModel()
    LoginScreen(vm, onBack = { nav.popBackStack() }, onAuthenticated = ::loggedIn, onRegister = { nav.navigate(Routes.REGISTER) })
   }
   composable(Routes.REGISTER) {
    val vm: RegisterViewModel = viewModel()
    RegisterScreen(vm, onBack = { nav.popBackStack() }, onDone = ::loggedIn)
   }
   composable(Routes.DASHBOARD) {
    val active = user
    if (active != null) DashboardScreen(active, onAbout = { nav.navigate(Routes.ABOUT) },
     onLogout = ::logOut, onProfile = { nav.navigate(Routes.PROFILE) },
     onCatalog = { nav.navigate(Routes.CATALOG) }, onCart = { nav.navigate(Routes.CART) })
    else LaunchedEffect(Unit) { nav.navigate(Routes.LOGIN) { popUpTo(Routes.DASHBOARD) { inclusive = true } } }
   }
   composable(Routes.PROFILE) {
    val active = user
    if (active != null) ProfileScreen(active, onBack = { nav.popBackStack() }, onLogout = ::logOut)
    else LaunchedEffect(Unit) { nav.navigate(Routes.LOGIN) }
   }
   composable(Routes.CATALOG) {
    val active = user
    if (active != null) CatalogScreen(active.alias, active.role == cl.oyl.frutossecos.domain.model.UserRole.ADMINISTRADOR,
     onBack = { nav.popBackStack() }, onCart = { nav.navigate(Routes.CART) })
   }
   composable(Routes.CART) {
    val active = user
    if (active != null && active.role == cl.oyl.frutossecos.domain.model.UserRole.CLIENTE)
     CartScreen(active.alias, onBack = { nav.popBackStack() })
   }
   composable(Routes.ABOUT) { AboutScreen(onBack = { nav.popBackStack() }) }
  }
 }
}