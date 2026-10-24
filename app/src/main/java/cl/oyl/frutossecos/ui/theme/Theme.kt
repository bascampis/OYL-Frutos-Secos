package cl.oyl.frutossecos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BlancoHueso = Color(0xFFF5F1E8)
private val BlancoSuave = Color(0xFFFCFAF6)
private val VerdeSuave = Color(0xFFE3EDE3)

private val OylColors = lightColorScheme(
    primary = OylGreen,
    onPrimary = Color.White,
    primaryContainer = VerdeSuave,
    onPrimaryContainer = OylDarkGreen,

    secondary = OylDarkGreen,
    onSecondary = Color.White,
    secondaryContainer = VerdeSuave,
    onSecondaryContainer = OylDarkGreen,

    tertiary = OylBrown,
    onTertiary = Color.White,
    tertiaryContainer = BlancoHueso,
    onTertiaryContainer = OylText,

    background = BlancoSuave,
    onBackground = OylText,

    surface = BlancoSuave,
    onSurface = OylText,
    surfaceVariant = BlancoHueso,
    onSurfaceVariant = Color(0xFF55584F),
    surfaceTint = Color.Transparent,

    surfaceDim = BlancoHueso,
    surfaceBright = BlancoSuave,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = BlancoSuave,
    surfaceContainer = BlancoHueso,
    surfaceContainerHigh = BlancoHueso,
    surfaceContainerHighest = BlancoHueso,

    outline = Color(0xFF85877D),
    outlineVariant = Color(0xFFDEDCD3),

    inverseSurface = OylDarkGreen,
    inverseOnSurface = BlancoSuave,
    inversePrimary = VerdeSuave,

    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    scrim = Color.Black
)

@Composable
fun OylTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OylColors,
        typography = OylTypography,
        content = content
    )
}