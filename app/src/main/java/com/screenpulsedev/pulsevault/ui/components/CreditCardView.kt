package com.screenpulsedev.pulsevault.ui.components

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.screenpulsedev.pulsevault.data.CardNetwork
import com.screenpulsedev.pulsevault.data.VaultCategory

/**
 * A realistic-looking card face — bank-colored (like a real physical card), with
 * the network logo in the corner and a "SANAL KART" badge for virtual cards.
 * Built only from plaintext display fields (bank, network, last 4 digits,
 * isVirtual) — no sensitive data is ever rendered here without unlocking.
 */
@Composable
fun CreditCardView(
    label: String,
    category: VaultCategory,
    network: CardNetwork,
    lastFourDigits: String,
    bank: String,
    isVirtual: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.75f)
            .clip(RoundedCornerShape(20.dp))
            .background(brush = gradientFor(bank, network))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                    fontSize = 11.sp
                )
                NetworkBadge(network)
            }
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
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text("TROY", color = Color(0xFFE30613), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        CardNetwork.AMEX -> Text("AMEX", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
        CardNetwork.OTHER -> Text("•••", color = Color.White.copy(alpha = 0.6f), fontSize = 16.sp, textAlign = TextAlign.End)
    }
}

/** Bank-brand-inspired gradients; falls back to a network-based gradient if bank is unset. */
private fun gradientFor(bank: String, network: CardNetwork): Brush {
    val bankColors: Pair<Color, Color>? = when (bank) {
        "Ziraat Bankası" -> Color(0xFFB3141F) to Color(0xFF6B0D14)
        "İş Bankası" -> Color(0xFF0B3D91) to Color(0xFF1E5FC4)
        "Garanti BBVA" -> Color(0xFF00854A) to Color(0xFF00A85E)
        "Yapı Kredi" -> Color(0xFF1B2A4A) to Color(0xFF2C4270)
        "Akbank" -> Color(0xFFB2001F) to Color(0xFF7A0015)
        "QNB Finansbank" -> Color(0xFF5A2D82) to Color(0xFF7B2FF7)
        "Halkbank" -> Color(0xFF0055A5) to Color(0xFF1E7FD1)
        "VakıfBank" -> Color(0xFFC8A415) to Color(0xFF8A6F0E)
        "TEB" -> Color(0xFF00695C) to Color(0xFF00A98F)
        "DenizBank" -> Color(0xFF005BAA) to Color(0xFF0089D6)
        "ING" -> Color(0xFFFF6200) to Color(0xFFCC4E00)
        else -> null
    }
    if (bankColors != null) return Brush.linearGradient(listOf(bankColors.first, bankColors.second))

    return when (network) {
        CardNetwork.VISA -> Brush.linearGradient(listOf(Color(0xFF4C5CFF), Color(0xFF7B2FF7)))
        CardNetwork.MASTERCARD -> Brush.linearGradient(listOf(Color(0xFF232526), Color(0xFF3A3D42)))
        CardNetwork.TROY -> Brush.linearGradient(listOf(Color(0xFFB3141F), Color(0xFF6B0D14)))
        CardNetwork.AMEX -> Brush.linearGradient(listOf(Color(0xFF1F6FE0), Color(0xFF2A9DF4)))
        CardNetwork.OTHER -> Brush.linearGradient(listOf(Color(0xFF5B3BFF), Color(0xFF9C4DFF)))
    }
}

private fun categoryLabel(category: VaultCategory): String = when (category) {
    VaultCategory.CREDIT_CARD -> "KREDİ KARTI"
    VaultCategory.BANK_ACCOUNT -> "BANKA HESABI"
    VaultCategory.PASSWORD -> "ŞİFRE"
    VaultCategory.NOTE -> "NOT"
}
