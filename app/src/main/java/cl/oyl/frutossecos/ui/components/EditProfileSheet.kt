package cl.oyl.frutossecos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.oyl.frutossecos.OylApplication
import cl.oyl.frutossecos.domain.validation.RegisterErrors
import cl.oyl.frutossecos.domain.validation.RegisterValidator
import cl.oyl.frutossecos.ui.theme.OylGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun SheetField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSheet(alias: String, displayName: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val repo = (context.applicationContext as OylApplication).authRepository
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(displayName) }
    var email by remember { mutableStateOf(alias) }
    var phone by remember { mutableStateOf("") }
    var attempted by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(alias) {
        try {
            repo.ensureProfile(alias, displayName)
            repo.getProfile(alias)?.let { name = it.name; email = it.email; phone = it.phone }
        } catch (_: Exception) {
        }
    }

    // Mismas reglas del registro; dirección y contraseña van con valores válidos de relleno.
    fun check() = RegisterValidator.validate(name, email, phone, "", "Relleno1", "Relleno1")
    val errors = if (attempted) check() else RegisterErrors()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Editar perfil", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            SheetField(name, { name = it; saved = false }, "Nombre completo", errors.name)
            SheetField(
                email, { email = it; saveError = null; saved = false }, "Correo electrónico",
                errors.email ?: saveError, KeyboardType.Email
            )
            SheetField(phone, { phone = it; saved = false }, "Teléfono", errors.phone, KeyboardType.Phone)

            if (saved) {
                Text("Datos guardados correctamente.", color = OylGreen, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    attempted = true
                    val c = check()
                    if (c.name == null && c.email == null && c.phone == null) {
                        scope.launch {
                            saving = true
                            try {
                                repo.updateContact(alias, name, email, phone)
                                saved = true
                                delay(900)
                                onDismiss()
                            } catch (e: Exception) {
                                saveError = e.message ?: "No se pudo guardar."
                            } finally {
                                saving = false
                            }
                        }
                    }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OylGreen)
            ) { Text(if (saving) "Guardando..." else "Guardar", fontWeight = FontWeight.Bold) }
        }
    }
}