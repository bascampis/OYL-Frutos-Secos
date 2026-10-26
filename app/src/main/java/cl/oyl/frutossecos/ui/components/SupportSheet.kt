package cl.oyl.frutossecos.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.oyl.frutossecos.ui.theme.OylGreen

// Datos de demostración: cámbialos por los del caso OYL si el enunciado los trae.
private const val SUPPORT_PHONE = "+56987654321"
private const val SUPPORT_PHONE_LABEL = "+56 9 8765 4321"
private const val SUPPORT_EMAIL = "soporte@oyl.cl"
private const val SUPPORT_INSTAGRAM = "oyl_frutossecos"
private const val SUPPORT_HOURS = "Lunes a viernes, 9:00 a 18:00"

private fun openExternal(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(
            context,
            "No hay una aplicación disponible para esta acción.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Ayuda y soporte",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Escríbenos o llámanos por tus pedidos y dudas sobre la app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(8.dp))

            SupportRow(Icons.Default.Call, "Llamar", SUPPORT_PHONE_LABEL) {
                openExternal(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$SUPPORT_PHONE")))
            }
            SupportRow(Icons.Default.Chat, "WhatsApp", SUPPORT_PHONE_LABEL) {
                openExternal(
                    context,
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${SUPPORT_PHONE.removePrefix("+")}"))
                )
            }
            SupportRow(Icons.Default.Email, "Correo", SUPPORT_EMAIL) {
                openExternal(
                    context,
                    Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$SUPPORT_EMAIL"))
                        .putExtra(Intent.EXTRA_SUBJECT, "Consulta OYL Frutos Secos")
                )
            }
            SupportRow(Icons.Default.Public, "Instagram", "@$SUPPORT_INSTAGRAM") {
                openExternal(
                    context,
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/$SUPPORT_INSTAGRAM"))
                )
            }
            SupportRow(Icons.Default.Schedule, "Horario de atención", SUPPORT_HOURS, onClick = null)

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                "Preguntas frecuentes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            FaqItem(
                "¿Cómo agrego productos al carrito?",
                "Abre el catálogo, entra a la ficha del producto, elige la cantidad y toca \"Agregar al carrito\"."
            )
            FaqItem(
                "¿Cómo escaneo el QR de un producto?",
                "Ve a Escáner en el menú inferior y toca \"Escanear QR ahora\". Si es la primera vez, la app te pedirá permiso para usar la cámara."
            )
            FaqItem(
                "¿Cómo cambio mi avatar?",
                "En Mi Perfil toca el lápiz sobre tu avatar. Puedes elegir uno o tomar una foto con la cámara."
            )
        }
    }
}

@Composable
private fun SupportRow(
    icon: ImageVector,
    title: String,
    detail: String,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = OylGreen)
        Column {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(question, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        if (expanded) {
            Text(
                answer,
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}