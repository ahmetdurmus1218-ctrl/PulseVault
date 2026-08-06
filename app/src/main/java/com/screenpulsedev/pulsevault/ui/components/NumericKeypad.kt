package com.screenpulsedev.pulsevault.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A fully custom on-screen digit pad — the system IME never gets involved when
 * this is used, so no third-party keyboard app can ever intercept what's typed.
 * Used for CVV entry (and the app PIN) since both are highly sensitive short
 * numeric secrets where this matters most.
 */
@Composable
fun NumericKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )
    rows.forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            row.forEach { key ->
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .then(
                            if (key.isNotEmpty()) Modifier.clickable {
                                if (key == "⌫") onBackspace() else onDigit(key)
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (key == "⌫") {
                        Icon(Icons.Filled.Backspace, contentDescription = "Sil")
                    } else if (key.isNotEmpty()) {
                        Text(key, style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}
