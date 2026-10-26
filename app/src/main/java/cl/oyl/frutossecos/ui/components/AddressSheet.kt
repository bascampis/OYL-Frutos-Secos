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
import cl.oyl.frutossecos.ui.theme.OylGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressSheet(alias: String, displayName: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as OylApplication
    val repo = app.authRepository
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(displayName) }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(alias) {
        try {
            repo.ensureProfile(alias, displayName)
            repo.getProfile(alias)?.let { p ->
                name = p.name
                phone = p.phone
                address = p.address
            }
        } catch (_: Exception) {
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Mis direcciones",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Estos datos se usan para el retiro o la entrega de tus pedidos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            OutlinedTextField(
                value = name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Nombre de contacto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; phoneError = null; saved = false },
                label = { Text("Teléfono") },
                isError = phoneError != null,
                supportingText = { phoneError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it; addressError = null; saved = false },
                label = { Text("Dirección de entrega") },
                isError = addressError != null,
                supportingText = { addressError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            if (saved) {
                Text(
                    "Datos guardados correctamente.",
                    color = OylGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    val digits = phone.count { it.isDigit() }
                    phoneError = if (digits < 8) "Teléfono: mínimo 8 dígitos." else null
                    addressError = if (address.trim().length < 5) "Ingresa una dirección de al menos 5 caracteres." else null
                    if (phoneError == null && addressError == null) {
                        scope.launch {
                            saving = true
                            try {
                                repo.updateProfile(alias, name, phone, address)
                                saved = true
                                delay(900)
                                onDismiss()
                            } catch (_: Exception) {
                                addressError = "No se pudo guardar. Intenta de nuevo."
                            } finally {
                                saving = false
                            }
                        }
                    }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OylGreen)
            ) {
                Text(if (saving) "Guardando..." else "Guardar", fontWeight = FontWeight.Bold)
            }
        }
    }
}