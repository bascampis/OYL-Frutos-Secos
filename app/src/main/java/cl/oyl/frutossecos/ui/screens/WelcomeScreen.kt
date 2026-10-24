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
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import cl.oyl.frutossecos.R
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun WelcomeScreen(onBegin: () -> Unit, onLogin: () -> Unit, onAbout: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    // Alto reservado arriba para el título (ajústalo si hace falta)
    val headerHeight = 190.dp

    Box(modifier = Modifier.fillMaxSize().background(OylCream)) {
        // Foto debajo del título, con borde superior difuminado hacia el crema
        Box(modifier = Modifier.fillMaxSize().padding(top = headerHeight)) {
            Image(
                painter = painterResource(id = R.drawable.frutos_imagen),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(Brush.verticalGradient(listOf(OylCream, Color.Transparent)))
            )
        }

        AnimatedVisibility(
            visible = isVisible,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(650)) + slideInVertically(tween(650)) { it / 6 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            )  {
                Spacer(modifier = Modifier.height(16.dp))
                Text("OYL", fontSize = 58.sp, fontWeight = FontWeight.ExtraBold, color = OylDarkGreen)
                Text("FRUTOS SECOS", style = MaterialTheme.typography.titleLarge, color = OylDarkGreen, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Productos naturales más cerca de ti",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = OylDarkGreen
                )

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 10.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BenefitRow("🌰", "Catálogo actualizado")
                        BenefitRow("📦", "Pedidos online")
                        BenefitRow("📍", "Retiros y despachos")
                        BenefitRow("🌿", "Productos saludables")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onBegin,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OylGreen)
                ) {
                    Text("Comenzar", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Text("Ya tengo una cuenta", fontWeight = FontWeight.SemiBold, color = OylDarkGreen)
                }
            }
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
