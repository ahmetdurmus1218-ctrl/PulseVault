package com.screenpulsedev.pulsevault.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity

/**
 * Classic card-flip: rotates around the Y axis and swaps content at the 90°
 * midpoint, so the "back" appears mirrored-correctly (not backwards).
 */
@Composable
fun CardFlip(
    isFlipped: Boolean,
    modifier: Modifier = Modifier,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 450),
        label = "cardFlip"
    )
    val density = LocalDensity.current

    androidx.compose.foundation.layout.Box(
        modifier = modifier.graphicsLayer {
            rotationY = rotation
            cameraDistance = 12f * density.density
        }
    ) {
        if (rotation <= 90f) {
            front()
        } else {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.graphicsLayer { rotationY = 180f }
            ) {
                back()
            }
        }
    }
}
