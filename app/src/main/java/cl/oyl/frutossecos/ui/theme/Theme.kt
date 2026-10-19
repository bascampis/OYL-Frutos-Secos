package cl.oyl.frutossecos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val OylColors = lightColorScheme(
    primary = OylGreen,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = OylMint,
    onPrimaryContainer = OylDarkGreen,
    secondary = OylBrown,
    background = OylCream,
    onBackground = OylText,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = OylText,
    surfaceVariant = OylMint
)

@Composable
fun OylTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = OylColors, typography = OylTypography, content = content)
}
