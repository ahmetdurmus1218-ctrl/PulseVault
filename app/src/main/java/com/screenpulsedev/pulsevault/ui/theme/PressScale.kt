package com.screenpulsedev.pulsevault.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Subtle "press and settle" scale — pass the returned [MutableInteractionSource]
 * into the same Button/Card's `interactionSource` param so this modifier can see
 * the real press state, and apply the returned Modifier to that same component:
 *
 * ```
 * val (scaleModifier, interactionSource) = rememberPressScale()
 * Button(onClick = ..., interactionSource = interactionSource, modifier = scaleModifier) { ... }
 * ```
 */
@Composable
fun rememberPressScale(pressedScale: Float = 0.96f): Pair<Modifier, MutableInteractionSource> {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "pressScale"
    )
    return Modifier.graphicsLayer(scaleX = scale, scaleY = scale) to interactionSource
}
