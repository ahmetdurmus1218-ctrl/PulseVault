package com.screenpulsedev.pulsevault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.screenpulsedev.pulsevault.data.VaultItemPayload
import kotlinx.coroutines.delay

/**
 * The back of the card. Every field starts MASKED. Tapping a masked field asks
 * for a fresh biometric/PIN check (via [onRequestAuth]) before revealing just
 * that one field — the rest stay hidden. A revealed field auto re-masks after
 * 15 seconds, so nothing stays decrypted-on-screen indefinitely.
 */
@Composable
fun CreditCardBackView(
    payload: VaultItemPayload,
    onCopy: (fieldLabel: String, value: String) -> Unit,
    onRequestAuth: (onGranted: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    // Bumping the nonce for a field key resets its 15s auto-hide timer.
    val revealNonce = remember { mutableStateMapOf<String, Int>() }

    fun reveal(key: String) {
        revealNonce[key] = (revealNonce[key] ?: 0) + 1
    }

    @Composable
    fun isRevealed(key: String): Boolean {
        val nonce = revealNonce[key] ?: 0
        if (nonce == 0) return false
        var revealed by remember(key, nonce) { mutableStateOf(true) }
        androidx.compose.runtime.LaunchedEffect(key, nonce) {
            delay(15_000)
            revealed = false
        }
        return revealed
    }

    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.75f)
            .shadow(elevation = 14.dp, shape = shape, ambientColor = Color.Black.copy(alpha = 0.35f), spotColor = Color.Black.copy(alpha = 0.5f))
            .clip(shape)
            .background(brush = Brush.linearGradient(listOf(Color(0xFF3A3D42), Color(0xFF232526), Color(0xFF141516))))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(Color.Black.copy(alpha = 0.85f))
            )
            Spacer(modifier = Modifier.height(14.dp))

            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                if (payload.number.isNotBlank()) {
                    val revealed = isRevealed("number")
                    MaskedField(
                        text = if (revealed) groupedNumber(payload.number) else "••••  ••••  ••••  ••••",
                        revealed = revealed,
                        fontSize = 17.sp,
                        onClick = {
                            if (revealed) onCopy("Kart Numarası", payload.number)
                            else onRequestAuth { reveal("number") }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (payload.holderName.isNotBlank()) {
                        val revealed = isRevealed("holder")
                        Column {
                            Text("KART SAHİBİ", color = Color.White.copy(alpha = 0.55f), fontSize = 9.sp)
                            MaskedField(
                                text = if (revealed) payload.holderName.uppercase() else "•••••••••",
                                revealed = revealed,
                                fontSize = 13.sp,
                                onClick = {
                                    if (revealed) onCopy("Kart Sahibi", payload.holderName)
                                    else onRequestAuth { reveal("holder") }
                                }
                            )
                        }
                    }
                    if (payload.expiry.isNotBlank()) {
                        val revealed = isRevealed("expiry")
                        Column {
                            Text("SKT", color = Color.White.copy(alpha = 0.55f), fontSize = 9.sp)
                            MaskedField(
                                text = if (revealed) payload.expiry else "••/••",
                                revealed = revealed,
                                fontSize = 13.sp,
                                onClick = {
                                    if (revealed) onCopy("Son Kullanma", payload.expiry)
                                    else onRequestAuth { reveal("expiry") }
                                }
                            )
                        }
                    }
                    if (payload.cvv.isNotBlank()) {
                        val revealed = isRevealed("cvv")
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (revealed) Color.White else Color.White.copy(alpha = 0.25f))
                                .clickable {
                                    if (revealed) onCopy("CVV", payload.cvv)
                                    else onRequestAuth { reveal("cvv") }
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (revealed) payload.cvv else "•••",
                                color = if (revealed) Color.Black else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (payload.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                val revealed = isRevealed("notes")
                MaskedField(
                    text = if (revealed) payload.notes else "•••••••••••••",
                    revealed = revealed,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    onClick = {
                        if (revealed) onCopy("Not", payload.notes)
                        else onRequestAuth { reveal("notes") }
                    },
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }
        }
    }
}

@Composable
private fun MaskedField(
    text: String,
    revealed: Boolean,
    fontSize: TextUnit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Row(
        modifier = modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!revealed) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(end = 4.dp).height(12.dp)
            )
        }
        Text(
            text = text,
            color = if (revealed) color else color.copy(alpha = 0.5f),
            fontSize = fontSize,
            fontWeight = if (revealed) FontWeight.Medium else FontWeight.Normal,
            letterSpacing = 1.sp
        )
    }
}

private fun groupedNumber(raw: String): String =
    raw.filter { it.isDigit() }.chunked(4).joinToString(" ")
