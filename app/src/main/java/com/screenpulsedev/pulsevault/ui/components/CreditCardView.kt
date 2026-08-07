package com.screenpulsedev.pulsevault.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.screenpulsedev.pulsevault.data.CardNetwork
import com.screenpulsedev.pulsevault.data.VaultCategory

/**
 * A realistic-looking card face — bank-colored, with a diagonal glass-like sheen,
 * a soft drop shadow, and an embossed EMV chip, so it reads as a physical card
 * rather than a flat color swatch. Built only from plaintext display fields
 * (bank, network, last 4 digits, isVirtual) — no sensitive data rendered here.
 */
@Composable
fun CreditCardView(
    label: String,
    category: VaultCategory,
    network: CardNetwork,
    lastFourDigits: String,
    bank: String,
    isVirtual: Boolean,
    modifier: Modifier = Modifier,
    showFullDetails: Boolean = true
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f)
            .shadow(elevation = 14.dp, shape = shape, ambientColor = Color.Black.copy(alpha = 0.35f), spotColor = Color.Black.copy(alpha = 0.5f))
            .clip(shape)
            .background(brush = gradientFor(bank, network))
    ) {
        // Diagonal glass sheen — a soft light band sweeping from top-left, like
        // light catching a laminated card surface.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colorStops = arrayOf(
                            0.0f to Color.White.copy(alpha = 0.16f),
                            0.22f to Color.White.copy(alpha = 0.04f),
                            0.4f to Color.Transparent,
                            1f to Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 900f)
                    )
                )
        )
        // Subtle vignette to deepen the edges, adding depth.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.12f)),
                        radius = 420f
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = bank.ifBlank { label },
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                    if (bank.isNotBlank() && label.isNotBlank() && label != bank) {
                        Text(
                            text = label,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isVirtual) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text("SANAL", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.Wifi,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp).padding(start = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            if (showFullDetails) {
                if (category == VaultCategory.CREDIT_CARD) {
                    EmvChip()
                }
                Spacer(modifier = Modifier.weight(1f))

                if (category == VaultCategory.CREDIT_CARD && lastFourDigits.isNotBlank()) {
                    Text(
                        text = "••••  ••••  ••••  $lastFourDigits",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = categoryLabel(category),
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
                    )
                    NetworkBadge(network)
                }
            }
        }
    }
}

/** A small embossed-looking gold EMV chip, like the metal contact pad on a real card. */
@Composable
private fun EmvChip() {
    Box(
        modifier = Modifier
            .size(width = 34.dp, height = 26.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(
                brush = Brush.linearGradient(
                    listOf(Color(0xFFE8CB7A), Color(0xFFB9962E), Color(0xFFE8CB7A))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeColor = Color(0xFF8A6D1E).copy(alpha = 0.55f)
            val stroke = Stroke(width = 1f)
            // Horizontal divider lines
            drawLine(strokeColor, Offset(0f, size.height * 0.35f), Offset(size.width, size.height * 0.35f), strokeWidth = 1f)
            drawLine(strokeColor, Offset(0f, size.height * 0.65f), Offset(size.width, size.height * 0.65f), strokeWidth = 1f)
            // Vertical divider lines
            drawLine(strokeColor, Offset(size.width * 0.33f, 0f), Offset(size.width * 0.33f, size.height), strokeWidth = 1f)
            drawLine(strokeColor, Offset(size.width * 0.66f, 0f), Offset(size.width * 0.66f, size.height), strokeWidth = 1f)
        }
    }
}

@Composable
private fun NetworkBadge(network: CardNetwork) {
    when (network) {
        CardNetwork.VISA -> Text("VISA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        CardNetwork.MASTERCARD -> Row {
            Box(Modifier.size(22.dp).clip(CircleShape).background(Color(0xFFEB001B).copy(alpha = 0.9f)))
            Box(
                Modifier.size(22.dp).offset(x = (-8).dp).clip(CircleShape)
                    .background(Color(0xFFF79E1B).copy(alpha = 0.9f))
            )
        }
        CardNetwork.TROY -> Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .padding(horizontal = 9.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            val troySlate = Color(0xFF4A4E58)
            val troyTeal = Color(0xFF1BADB5)
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = troySlate)) { append("tr") }
                    withStyle(SpanStyle(color = troyTeal)) { append("o") }
                    withStyle(SpanStyle(color = troySlate)) { append("y") }
                },
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                maxLines = 1
            )
        }
        CardNetwork.AMEX -> Text("AMEX", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
        CardNetwork.OTHER -> Text("•••", color = Color.White.copy(alpha = 0.6f), fontSize = 16.sp, textAlign = TextAlign.End)
    }
}

/** Bank-brand-inspired gradients, now with a third tonal stop for extra depth. */
private fun gradientFor(bank: String, network: CardNetwork): Brush {
    val bankColors: Triple<Color, Color, Color>? = when (bank) {
        "Ziraat Bankası" -> Triple(Color(0xFFCB1A26), Color(0xFF8A0F19), Color(0xFF5A0A10))
        "İş Bankası" -> Triple(Color(0xFF1449AD), Color(0xFF0B3D91), Color(0xFF082A66))
        "Garanti BBVA" -> Triple(Color(0xFF00A85E), Color(0xFF00854A), Color(0xFF005C33))
        "Yapı Kredi" -> Triple(Color(0xFF32467A), Color(0xFF1B2A4A), Color(0xFF121C33))
        "Akbank" -> Triple(Color(0xFFCB1030), Color(0xFFB2001F), Color(0xFF700014))
        "QNB Finansbank" -> Triple(Color(0xFF8B5FE0), Color(0xFF5A2D82), Color(0xFF3B1D5B))
        "Halkbank" -> Triple(Color(0xFF1E7FD1), Color(0xFF0055A5), Color(0xFF003C77))
        "VakıfBank" -> Triple(Color(0xFFE0BC3D), Color(0xFFC8A415), Color(0xFF7A600D))
        "TEB" -> Triple(Color(0xFF00A98F), Color(0xFF00695C), Color(0xFF00463E))
        "DenizBank" -> Triple(Color(0xFF2E9FEC), Color(0xFF005BAA), Color(0xFF003E77))
        "ING" -> Triple(Color(0xFFFF8A3D), Color(0xFFFF6200), Color(0xFF993B00))
        else -> null
    }
    if (bankColors != null) {
        return Brush.linearGradient(listOf(bankColors.first, bankColors.second, bankColors.third))
    }

    return when (network) {
        CardNetwork.VISA -> Brush.linearGradient(listOf(Color(0xFF6E7CFF), Color(0xFF4C5CFF), Color(0xFF5A1FD9)))
        CardNetwork.MASTERCARD -> Brush.linearGradient(listOf(Color(0xFF3A3D42), Color(0xFF232526), Color(0xFF141516)))
        CardNetwork.TROY -> Brush.linearGradient(listOf(Color(0xFFCB1A26), Color(0xFFB3141F), Color(0xFF5A0A10)))
        CardNetwork.AMEX -> Brush.linearGradient(listOf(Color(0xFF4FA0F0), Color(0xFF1F6FE0), Color(0xFF124A99)))
        CardNetwork.OTHER -> Brush.linearGradient(listOf(Color(0xFF7B5CFF), Color(0xFF5B3BFF), Color(0xFF3A1F9C)))
    }
}

private fun categoryLabel(category: VaultCategory): String = when (category) {
    VaultCategory.CREDIT_CARD -> "KREDİ KARTI"
    VaultCategory.BANK_ACCOUNT -> "BANKA HESABI"
    VaultCategory.PASSWORD -> "ŞİFRE"
    VaultCategory.NOTE -> "NOT"
}
