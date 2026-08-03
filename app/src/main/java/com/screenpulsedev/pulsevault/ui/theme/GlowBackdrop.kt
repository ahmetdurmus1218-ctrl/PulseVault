package com.screenpulsedev.pulsevault.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * A soft ambient blue/purple glow behind the screen content, echoing the
 * reference design's lit-from-behind look — approximated with layered radial
 * gradients since Compose has no cheap true blur for a background blob.
 */
@Composable
fun GlowBackdrop(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4C5CFF).copy(alpha = 0.18f),
                        Color(0xFF4C5CFF).copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = Offset(0.5f, 0.05f),
                    radius = 900f
                )
            )
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF7B2FF7).copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(0.9f, 0.85f),
                    radius = 700f
                )
            )
    ) {
        content()
    }
}
