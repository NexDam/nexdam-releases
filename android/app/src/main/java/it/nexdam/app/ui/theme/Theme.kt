package it.nexdam.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NexDamColors = darkColorScheme(
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    primary = Primary,
    onBackground = OnBackground,
    onSurface = OnSurface,
    error = Error
)

@Composable
fun NexDamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NexDamColors,
        content = content
    )
}
