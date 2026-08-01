package com.screenpulsedev.pulsevault.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PulseDark = darkColorScheme(
    primary = Color(0xFF7C9CFF),
    secondary = Color(0xFF9C7CFF),
    background = Color(0xFF10121A),
    surface = Color(0xFF181B26)
)

private val PulseLight = lightColorScheme(
    primary = Color(0xFF3B5BFF),
    secondary = Color(0xFF6A3BFF),
    background = Color(0xFFF6F7FB),
    surface = Color.White
)

@Composable
fun PulseVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) PulseDark else PulseLight,
        content = content
    )
}
