package cl.oyl.frutossecos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.oyl.frutossecos.ui.theme.OylCream
import cl.oyl.frutossecos.ui.theme.OylDarkGreen
import cl.oyl.frutossecos.ui.theme.OylMint

@Composable
fun WelcomeScreen(onBegin: () -> Unit, onAbout: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    Column(
        modifier = Modifier.fillMaxSize().background(OylCream)
            .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 24.dp),
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
                        modifier = Modifier.height(124.dp).fillMaxWidth(0.60f)
                            .background(OylMint, RoundedCornerShape(42.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌰", fontSize = 66.sp)
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text("OYL", fontSize = 58.sp, fontWeight = FontWeight.ExtraBold, color = OylDarkGreen)
                Text("FRUTOS SECOS", style = MaterialTheme.typography.titleLarge, color = OylDarkGreen)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Productos naturales, más cerca de ti",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(30.dp))
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        BenefitRow("✓", "Frutos secos, semillas y deshidratados")
                        BenefitRow("✓", "Catálogo y pedidos en una sola aplicación")
                        BenefitRow("✓", "Gestión de inventario para el negocio")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        Button(onClick = onBegin, modifier = Modifier.fillMaxWidth().height(54.dp)) {
            Text("Comenzar", fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(onClick = onAbout, modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Text("Acerca de esta demostración")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Proyecto académico · Versión base 01",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun BenefitRow(mark: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier.background(OylMint, CircleShape).padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(mark, fontWeight = FontWeight.Bold, color = OylDarkGreen)
        }
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}
