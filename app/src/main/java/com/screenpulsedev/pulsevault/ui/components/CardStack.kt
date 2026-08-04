package com.screenpulsedev.pulsevault.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.screenpulsedev.pulsevault.data.VaultItem
import kotlinx.coroutines.launch

/**
 * Wallet-style stack: cards overlap with just a sliver of each peeking out behind
 * the one in front. Dragging down (or tapping) fans them out. Once fully expanded,
 * the drag-to-fan gesture is switched OFF entirely — otherwise it kept swallowing
 * the outer list's normal scroll gestures, making it impossible to scroll past the
 * stack. Collapsing back is a deliberate tap on the small "Kartları Topla" pill
 * instead, so scrolling and collapsing can never be confused with each other.
 */
@Composable
fun CardStack(
    items: List<VaultItem>,
    onItemClick: (VaultItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }

    val collapsedStep = 42.dp
    val expandedGap = 14.dp

    fun settle(target: Float) {
        scope.launch {
            progress.animateTo(target, animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f))
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cardWidth = maxWidth
            val cardHeight = cardWidth / 1.75f
            val expandedStep = cardHeight + expandedGap
            val n = items.size

            // Cap how tall the COLLAPSED stack can ever get, regardless of how many
            // cards exist — otherwise many cards would each add a fixed peek and
            // the "collapsed" stack would grow to fill (and overflow) the screen.
            val maxCollapsedPeekTotal = 168.dp
            val collapsedStepNow = if (n <= 1) collapsedStep
            else minOf(collapsedStep, maxCollapsedPeekTotal / (n - 1))

            val stepNow = androidx.compose.ui.unit.lerp(collapsedStepNow, expandedStep, progress.value)
            val containerHeight = cardHeight + stepNow * (n - 1).coerceAtLeast(0)
            val isFullyExpanded = progress.value > 0.98f

            val dragModifier = if (isFullyExpanded) {
                Modifier // no gesture capture at all once expanded — the parent list scrolls freely
            } else {
                Modifier.pointerInput(n) {
                    val totalDragRangePx = with(density) {
                        (expandedStep - collapsedStepNow).toPx() * (n - 1).coerceAtLeast(1)
                    }
                    detectVerticalDragGestures(
                        onDragEnd = { settle(if (progress.value > 0.35f) 1f else 0f) },
                        onDragCancel = { settle(if (progress.value > 0.35f) 1f else 0f) }
                    ) { change, dragAmount ->
                        change.consume()
                        val next = (progress.value + dragAmount / totalDragRangePx).coerceIn(0f, 0.98f)
                        scope.launch { progress.snapTo(next) }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(containerHeight)
                    .then(dragModifier)
            ) {
                items.forEachIndexed { index, item ->
                    val offsetY = stepNow * index
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(cardHeight)
                            .offset(y = offsetY)
                            .zIndex((n - index).toFloat())
                    ) {
                        val (scale, interaction) = com.screenpulsedev.pulsevault.ui.theme.rememberPressScale()
                        CreditCardView(
                            label = item.label,
                            category = item.category,
                            network = item.network,
                            lastFourDigits = item.lastFourDigits,
                            bank = item.bank,
                            isVirtual = item.isVirtual,
                            modifier = scale.then(
                                Modifier.clickable(
                                    interactionSource = interaction,
                                    indication = androidx.compose.foundation.LocalIndication.current
                                ) {
                                    if (progress.value > 0.5f) {
                                        onItemClick(item)
                                    } else {
                                        settle(1f)
                                    }
                                }
                            )
                        )
                    }
                }

                // Small affordance hint on the very top card, only while collapsed.
                if (progress.value < 0.15f && n > 1) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = (-4).dp)
                            .zIndex((n + 1).toFloat())
                    )
                }
            }
        }

        // Deliberate, unambiguous way to re-collapse — only shown once expanded.
        AnimatedVisibility(
            visible = progress.value > 0.5f,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { settle(0f) }
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null, modifier = Modifier.height(16.dp))
                Text("Kartları Topla", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
