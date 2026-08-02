package com.screenpulsedev.pulsevault.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
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
    primary = Color(0xFF8B9CFF),
    onPrimary = Color(0xFF15173A),
    primaryContainer = Color(0xFF37398F),
    secondary = Color(0xFFB08BFF),
    tertiary = Color(0xFF7BD9C9),
    background = Color(0xFF0E0F17),
    surface = Color(0xFF171923),
    surfaceVariant = Color(0xFF20232F),
    error = Color(0xFFFF6B6B),
    onSurfaceVariant = Color(0xFFA7ABC2)
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
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) PulseDark else PulseLight,
        shapes = PulseShapes,
        typography = PulseTypography,
        content = content
    )
}
