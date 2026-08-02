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
 * A realistic-looking card face for the vault list — the same visual language as a
 * physical card (network logo, masked number, gradient by network) but built only
 * from the two plaintext display fields (network + last 4 digits). No sensitive
 * data (full number, CVV, holder name) is ever rendered here without unlocking.
 */
@Composable
fun CreditCardView(
    label: String,
    category: VaultCategory,
    network: CardNetwork,
    lastFourDigits: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.75f)
            .clip(RoundedCornerShape(20.dp))
            .background(brush = gradientFor(network))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.Filled.Wifi,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(22.dp)
                )
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
        CardNetwork.VISA -> Text(
            "VISA",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        CardNetwork.MASTERCARD -> Row {
            Box(
                Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEB001B).copy(alpha = 0.9f))
            )
            Box(
                Modifier
                    .size(22.dp)
                    .offset(x = (-8).dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF79E1B).copy(alpha = 0.9f))
            )
        }
        CardNetwork.AMEX -> Text(
            "AMEX",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 1.sp
        )
        CardNetwork.OTHER -> Text(
            "•••",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 16.sp,
            textAlign = TextAlign.End
        )
    }
}

private fun gradientFor(network: CardNetwork): Brush = when (network) {
    CardNetwork.VISA -> Brush.linearGradient(listOf(Color(0xFF4C5CFF), Color(0xFF7B2FF7)))
    CardNetwork.MASTERCARD -> Brush.linearGradient(listOf(Color(0xFF232526), Color(0xFF3A3D42)))
    CardNetwork.AMEX -> Brush.linearGradient(listOf(Color(0xFF1F6FE0), Color(0xFF2A9DF4)))
    CardNetwork.OTHER -> Brush.linearGradient(listOf(Color(0xFF5B3BFF), Color(0xFF9C4DFF)))
}

private fun categoryLabel(category: VaultCategory): String = when (category) {
    VaultCategory.CREDIT_CARD -> "KREDİ KARTI"
    VaultCategory.BANK_ACCOUNT -> "BANKA HESABI"
    VaultCategory.PASSWORD -> "ŞİFRE"
    VaultCategory.NOTE -> "NOT"
}
