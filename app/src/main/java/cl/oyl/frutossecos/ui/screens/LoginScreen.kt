package cl.oyl.frutossecos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cl.oyl.frutossecos.domain.model.DemoUser
import cl.oyl.frutossecos.domain.model.UserRole
import cl.oyl.frutossecos.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: LoginViewModel, onBack: () -> Unit, onAuthenticated: (DemoUser) -> Unit, onRegister: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    var showPassword by remember { mutableStateOf(false) }
    val submit = { viewModel.signIn(onAuthenticated) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inicio de sesión") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Bienvenido a OYL", style = MaterialTheme.typography.headlineMedium)
            Text("Selecciona tu tipo de cuenta e ingresa para explorar la versión de demostración.")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = state.role == UserRole.CLIENTE,
                    onClick = { viewModel.selectRole(UserRole.CLIENTE) },
                    label = { Text("Cliente") }
                )
                FilterChip(
                    selected = state.role == UserRole.ADMINISTRADOR,
                    onClick = { viewModel.selectRole(UserRole.ADMINISTRADOR) },
                    label = { Text("Administrador") }
                )
            }
            OutlinedTextField(
                value = state.alias,
                onValueChange = viewModel::updateAlias,
                label = { Text("Alias o correo") },
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                isError = state.aliasError != null,
                supportingText = { if (state.aliasError != null) Text(requireNotNull(state.aliasError)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::updatePassword,
                label = { Text("Contraseña") },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                isError = state.passwordError != null,
                supportingText = { if (state.passwordError != null) Text(requireNotNull(state.passwordError)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                modifier = Modifier.fillMaxWidth()
            )
            AnimatedVisibility(visible = state.generalError != null) {
                Text(
                    text = state.generalError.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(
                onClick = submit,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                if (state.isLoading) CircularProgressIndicator(strokeWidth = 2.dp)
                else Text("Iniciar sesión", fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Acceso académico de prueba", style = MaterialTheme.typography.titleMedium)
            Text(
                "No hay servidor conectado ni cuentas reales en ZIP 01. Selecciona un rol y carga sus datos ficticios.",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = viewModel::useDemoCredentials) {
                Text("Rellenar credenciales de demostración")
            }
            Text(
                "Cliente: cliente  ·  Administrador: admin@admin.cl",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
