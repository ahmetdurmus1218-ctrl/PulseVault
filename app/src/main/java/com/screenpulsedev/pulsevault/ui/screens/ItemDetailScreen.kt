package com.screenpulsedev.pulsevault.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.screenpulsedev.pulsevault.data.VaultItemPayload

@Composable
fun ItemDetailScreen(
    label: String,
    payload: VaultItemPayload,
    onCopy: (fieldLabel: String, value: String) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(topBar = { TopAppBar(title = { Text(label) }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            DetailRow("Kart Sahibi", payload.holderName, onCopy)
            DetailRow("Numara", payload.number, onCopy)
            DetailRow("Son Kullanma", payload.expiry, onCopy)
            DetailRow("CVV", payload.cvv, onCopy)
            if (payload.notes.isNotBlank()) {
                DetailRow("Not", payload.notes, onCopy)
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "Kopyalanan veriler 30 saniye sonra panodan otomatik silinir.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Delete, contentDescription = null)
                Spacer(Modifier.height(0.dp))
                Text("  Sil")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Geri") }
        }
    }
}

@Composable
private fun DetailRow(fieldLabel: String, value: String, onCopy: (String, String) -> Unit) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(fieldLabel, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
        IconButton(onClick = { onCopy(fieldLabel, value) }) {
            Icon(Icons.Filled.ContentCopy, contentDescription = "Kopyala")
        }
    }
}
