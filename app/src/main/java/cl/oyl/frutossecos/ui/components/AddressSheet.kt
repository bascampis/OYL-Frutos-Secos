package cl.oyl.frutossecos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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

private val REGIONS = listOf(
    "Arica y Parinacota", "Tarapacá", "Antofagasta", "Atacama", "Coquimbo", "Valparaíso",
    "Metropolitana de Santiago", "Libertador General Bernardo O'Higgins", "Maule", "Ñuble",
    "Biobío", "La Araucanía", "Los Ríos", "Los Lagos",
    "Aysén del General Carlos Ibáñez del Campo", "Magallanes y de la Antártica Chilena"
)

/** Separa "Calle 123, Región" en (calle, número, región). Si no calza, devuelve todo como calle. */
private fun parseAddress(raw: String): Triple<String, String, String> {
    val idx = raw.lastIndexOf(", ")
    if (idx < 0) return Triple(raw, "", "")
    val region = raw.substring(idx + 2)
    if (region !in REGIONS) return Triple(raw, "", "")
    val left = raw.substring(0, idx)
    val m = Regex("^(.*?)\\s+(\\d+)$").find(left)
    return if (m != null) Triple(m.groupValues[1], m.groupValues[2], region) else Triple(left, "", region)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressSheet(alias: String, displayName: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val repo = (context.applicationContext as OylApplication).authRepository
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var street by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var name by remember { mutableStateOf(displayName) }
    var phone by remember { mutableStateOf("") }
    var streetError by remember { mutableStateOf<String?>(null) }
    var numberError by remember { mutableStateOf<String?>(null) }
    var regionError by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(alias) {
        try {
            repo.ensureProfile(alias, displayName)
            repo.getProfile(alias)?.let { p ->
                name = p.name
                phone = p.phone
                val (s, n, r) = parseAddress(p.address)
                street = s; number = n; region = r
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Mis direcciones", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Esta dirección se usa para el retiro o la entrega de tus pedidos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            SheetField(street, { street = it; streetError = null; saved = false }, "Calle", streetError)
            SheetField(
                number, { number = it.filter { c -> c.isDigit() }; numberError = null; saved = false },
                "Número de calle", numberError, KeyboardType.Number
            )

            Box {
                OutlinedTextField(
                    value = region,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Región") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    isError = regionError != null,
                    supportingText = { regionError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    REGIONS.forEach { r ->
                        DropdownMenuItem(
                            text = { Text(r) },
                            onClick = { region = r; regionError = null; saved = false; expanded = false }
                        )
                    }
                }
            }

            // Datos de contacto: solo lectura, se cambian en "Editar perfil".
            Text(
                "Contacto: $name · ${phone.ifBlank { "sin teléfono" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            if (saved) {
                Text("Dirección guardada correctamente.", color = OylGreen, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    streetError = if (street.count { it.isLetter() } < 3) "La calle debe tener al menos 3 letras." else null
                    numberError = if (!Regex("^\\d{1,6}$").matches(number)) "Ingresa el número de calle." else null
                    regionError = if (region.isBlank()) "Selecciona una región." else null
                    if (streetError == null && numberError == null && regionError == null) {
                        scope.launch {
                            saving = true
                            try {
                                repo.updateProfile(alias, name, phone, "${street.trim()} ${number.trim()}, $region")
                                saved = true
                                delay(900)
                                onDismiss()
                            } catch (_: Exception) {
                                streetError = "No se pudo guardar. Intenta de nuevo."
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