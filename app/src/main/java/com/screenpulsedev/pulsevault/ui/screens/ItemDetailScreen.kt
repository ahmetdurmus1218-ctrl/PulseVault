package com.screenpulsedev.pulsevault.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.screenpulsedev.pulsevault.data.CardNetwork
import com.screenpulsedev.pulsevault.data.VaultCategory
import com.screenpulsedev.pulsevault.data.VaultItemPayload
import com.screenpulsedev.pulsevault.ui.components.CardFlip
import com.screenpulsedev.pulsevault.ui.components.CreditCardBackView
import com.screenpulsedev.pulsevault.ui.components.CreditCardView

@Composable
fun ItemDetailScreen(
    label: String,
    category: VaultCategory,
    network: CardNetwork,
    lastFourDigits: String,
    bank: String,
    isVirtual: Boolean,
    payload: VaultItemPayload,
    onCopy: (fieldLabel: String, value: String) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isFlipped by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text(label) }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            CardFlip(
                isFlipped = isFlipped,
                modifier = Modifier.fillMaxWidth(),
                front = {
                    CreditCardView(
                        label = label,
                        category = category,
                        network = network,
                        lastFourDigits = lastFourDigits,
                        bank = bank,
                        isVirtual = isVirtual
                    )
                },
                back = { CreditCardBackView(payload = payload) }
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { isFlipped = !isFlipped },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Flip, contentDescription = null)
                Text(if (isFlipped) "  Ön Yüzü Göster" else "  Arka Yüzü Göster")
            }

            Spacer(Modifier.height(20.dp))
            Text("Bilgileri Kopyala", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            DetailRow("Kart Sahibi", payload.holderName, onCopy)
            DetailRow("Numara", payload.number, onCopy)
            DetailRow("Son Kullanma", payload.expiry, onCopy)
            DetailRow("CVV", payload.cvv, onCopy)
            if (payload.notes.isNotBlank()) {
                DetailRow("Not", payload.notes, onCopy)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Kopyalanan veriler 30 saniye sonra panodan otomatik silinir.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null)
                Text("  Sil")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Geri") }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Kaydı sil") },
            text = { Text("\"$label\" kalıcı olarak silinecek. Bu işlem geri alınamaz.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text("Sil", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Vazgeç") }
            }
        )
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
