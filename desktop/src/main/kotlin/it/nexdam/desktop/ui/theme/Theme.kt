package it.nexdam.desktop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Background  = Color(0xFF0D1117)
val Surface     = Color(0xFF161B22)
val SurfaceVar  = Color(0xFF1C2128)
val Primary     = Color(0xFF58A6FF)
val OnBg        = Color(0xFFE6EDF3)
val OnSurface   = Color(0xFFCDD9E5)
val Muted       = Color(0xFF8B949E)
val Success     = Color(0xFF3FB950)
val Warning     = Color(0xFFD29922)
val Danger      = Color(0xFFF85149)
val Divider     = Color(0xFF30363D)

private val Colors = darkColorScheme(
    background   = Background,
    surface      = Surface,
    surfaceVariant = SurfaceVar,
    primary      = Primary,
    onBackground = OnBg,
    onSurface    = OnSurface,
    error        = Danger
)

@Composable
fun NexDamTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
