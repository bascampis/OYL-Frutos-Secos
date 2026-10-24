package cl.oyl.frutossecos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.oyl.frutossecos.ui.theme.OylCream
import cl.oyl.frutossecos.ui.theme.OylDarkGreen
import cl.oyl.frutossecos.ui.theme.OylGreen
import cl.oyl.frutossecos.ui.theme.OylMint

@Composable
fun WelcomeScreen(onBegin: () -> Unit, onLogin: () -> Unit, onAbout: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OylCream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(650)) + slideInVertically(tween(650)) { it / 4 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.height(132.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .height(124.dp)
                            .fillMaxWidth(0.60f)
                            .background(OylMint, RoundedCornerShape(42.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌰", fontSize = 66.sp)
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text("OYL", fontSize = 58.sp, fontWeight = FontWeight.ExtraBold, color = OylDarkGreen)
                Text("FRUTOS SECOS", style = MaterialTheme.typography.titleLarge, color = OylDarkGreen, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Productos naturales más cerca de ti",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = OylDarkGreen
                )
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        BenefitRow("🌰", "Catálogo actualizado")
                        BenefitRow("📦", "Pedidos online")
                        BenefitRow("📍", "Retiros y despachos")
                        BenefitRow("🌿", "Productos saludables")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onBegin,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OylGreen)
        ) {
            Text("Comenzar", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Ya tengo una cuenta", fontWeight = FontWeight.SemiBold, color = OylDarkGreen)
        }
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(onClick = onAbout) {
            Text("Acerca de esta demostración", color = OylDarkGreen)
        }
    }
}

@Composable
private fun BenefitRow(icon: String, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(OylMint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 16.sp)
        }
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = OylDarkGreen)
    }
}
