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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.screenpulsedev.pulsevault.data.VaultItemPayload

/**
 * The back of the card. Every field (number, holder, expiry, CVV) is directly
 * tappable to copy — no separate list of buttons needed below the card.
 */
@Composable
fun CreditCardBackView(
    payload: VaultItemPayload,
    onCopy: (fieldLabel: String, value: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.75f)
            .clip(RoundedCornerShape(20.dp))
            .background(brush = Brush.linearGradient(listOf(Color(0xFF232526), Color(0xFF3A3D42))))
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
                    Text(
                        text = groupedNumber(payload.number),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                        modifier = Modifier.clickable {
                            onCopy("Kart Numarası", payload.number)
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (payload.holderName.isNotBlank()) {
                        Column(
                            modifier = Modifier.clickable {
                                onCopy("Kart Sahibi", payload.holderName)
                            }
                        ) {
                            Text("KART SAHİBİ", color = Color.White.copy(alpha = 0.55f), fontSize = 9.sp)
                            Text(payload.holderName.uppercase(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (payload.expiry.isNotBlank()) {
                        Column(
                            modifier = Modifier.clickable {
                                onCopy("Son Kullanma", payload.expiry)
                            }
                        ) {
                            Text("SKT", color = Color.White.copy(alpha = 0.55f), fontSize = 9.sp)
                            Text(payload.expiry, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (payload.cvv.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White)
                                .clickable { onCopy("CVV", payload.cvv) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(payload.cvv, color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (payload.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = payload.notes,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .clickable { onCopy("Not", payload.notes) }
                )
            }
        }
    }
}

private fun groupedNumber(raw: String): String =
    raw.filter { it.isDigit() }.chunked(4).joinToString(" ")
