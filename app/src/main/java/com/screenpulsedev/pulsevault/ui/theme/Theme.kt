package com.screenpulsedev.pulsevault.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PulseDark = darkColorScheme(
    primary = Color(0xFF5B7CFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF26305E),
    secondary = Color(0xFF7B5CFF),
    tertiary = Color(0xFF4FD1C5),
    background = Color(0xFF090B14),
    surface = Color(0xFF111422),
    surfaceVariant = Color(0xFF1A1E33),
    error = Color(0xFFFF6B6B),
    onSurfaceVariant = Color(0xFF9CA3C4)
)

private val PulseLight = lightColorScheme(
    primary = Color(0xFF4C5CFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E4FF),
    secondary = Color(0xFF7B2FF7),
    tertiary = Color(0xFF00A98F),
    background = Color(0xFFF7F7FC),
    surface = Color.White,
    surfaceVariant = Color(0xFFEEEFF9),
    error = Color(0xFFE0435D),
    onSurfaceVariant = Color(0xFF5C5F72)
)

/** Rounder, more modern corner radii than Material3's defaults. */
private val PulseShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val PulseTypography = Typography(
    headlineMedium = Typography().headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp
    ),
    titleLarge = Typography().titleLarge.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = Typography().bodyLarge.copy(letterSpacing = 0.1.sp)
)

@Composable
fun PulseVaultTheme(
    darkTheme: Boolean = true, // Always dark — matches the app's visual identity
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) PulseDark else PulseLight,
        shapes = PulseShapes,
        typography = PulseTypography,
        content = content
    )
}
