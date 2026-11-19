package app.fitforge.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import app.fitforge.data.prefs.ThemeMode

private val DarkColors = darkColorScheme(
    primary = Ember,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3A1D10),
    onPrimaryContainer = Color(0xFFFFD2BF),
    secondary = Lime,
    onSecondary = Ink,
    secondaryContainer = Color(0xFF2C3410),
    onSecondaryContainer = Lime,
    tertiary = Sky,
    onTertiary = Color.White,
    background = Ink,
    onBackground = Snow,
    surface = Ink,
    onSurface = Snow,
    surfaceVariant = Slate,
    onSurfaceVariant = Mist,
    surfaceContainerLowest = Ink,
    surfaceContainerLow = Graphite,
    surfaceContainer = Graphite,
    surfaceContainerHigh = Slate,
    surfaceContainerHighest = SlateHigh,
    outline = Color(0xFF3A3F4B),
    outlineVariant = Color(0xFF2A2E37),
    error = Rose,
)

private val LightColors = lightColorScheme(
    primary = EmberDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE1D4),
    onPrimaryContainer = Color(0xFF5A1D00),
    secondary = Color(0xFF5E7A00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5F7A8),
    onSecondaryContainer = Color(0xFF283400),
    tertiary = Color(0xFF3A6BE0),
    background = Paper,
    onBackground = Charcoal,
    surface = Paper,
    onSurface = Charcoal,
    surfaceVariant = PaperHigh,
    onSurfaceVariant = Smoke,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = PaperHigh,
    surfaceContainerHighest = Color(0xFFE2E4EA),
    outline = Color(0xFFC9CCD4),
    outlineVariant = Color(0xFFE2E4EA),
    error = Color(0xFFD7263D),
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun FitForgeTheme(mode: ThemeMode = ThemeMode.ESCURO, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.ESCURO -> true
        ThemeMode.CLARO -> false
        ThemeMode.SISTEMA -> isSystemInDarkTheme()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
