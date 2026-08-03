package com.screenpulsedev.pulsevault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.screenpulsedev.pulsevault.ui.theme.rememberPressScale

/** A small pill-shaped button with a soft drop shadow and press-in feel — not full-width. */
@Composable
fun SmallActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val (scale, interaction) = rememberPressScale(pressedScale = 0.93f)
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = modifier
            .then(scale)
            .shadow(elevation = 6.dp, shape = shape, ambientColor = Color.Black.copy(alpha = 0.3f))
            .clip(shape)
            .background(containerColor)
            .clickable(
                interactionSource = interaction,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.padding(end = 6.dp))
        Text(label, color = contentColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
