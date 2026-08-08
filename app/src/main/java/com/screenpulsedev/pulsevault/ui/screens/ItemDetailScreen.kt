package com.screenpulsedev.pulsevault.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.screenpulsedev.pulsevault.data.CardNetwork
import com.screenpulsedev.pulsevault.data.VaultCategory
import com.screenpulsedev.pulsevault.data.VaultItemPayload
import com.screenpulsedev.pulsevault.ui.components.CardFlip
import com.screenpulsedev.pulsevault.ui.components.CreditCardBackView
import com.screenpulsedev.pulsevault.ui.components.CreditCardView
import com.screenpulsedev.pulsevault.ui.components.SmallActionButton

@Composable
fun ItemDetailScreen(
    label: String,
    category: VaultCategory,
    network: CardNetwork,
    lastFourDigits: String,
    bank: String,
    isVirtual: Boolean,
    isFavorite: Boolean,
    payload: VaultItemPayload,
    onCopy: (fieldLabel: String, value: String) -> Unit,
    onRequestAuth: (onGranted: () -> Unit) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isFlipped by remember { mutableStateOf(false) }
    var favoriteNow by remember(isFavorite) { mutableStateOf(isFavorite) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(label) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { favoriteNow = !favoriteNow; onToggleFavorite() }) {
                        Icon(
                            if (favoriteNow) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Favori",
                            tint = if (favoriteNow) androidx.compose.ui.graphics.Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (com.screenpulsedev.pulsevault.auth.hasActiveAccessibilityServices(context)) {
                androidx.compose.material3.Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "⚠ Ekranı okuyabilecek bir servis aktif — dikkatli ol",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
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
                back = { CreditCardBackView(payload = payload, onCopy = onCopy, onRequestAuth = onRequestAuth) }
            )
            Spacer(Modifier.height(12.dp))

            // Compact, centered pill button instead of a full-width bar.
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SmallActionButton(
                    icon = Icons.Filled.Flip,
                    label = if (isFlipped) "Ön Yüz" else "Arka Yüz",
                    onClick = { isFlipped = !isFlipped },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Kopyalanan veriler kısa süre sonra panodan otomatik silinir (Ayarlar'dan değiştirilebilir).",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                SmallActionButton(
                    icon = Icons.Filled.Edit,
                    label = "Düzenle",
                    onClick = onEdit
                )
                Spacer(Modifier.width(12.dp))
                SmallActionButton(
                    icon = Icons.Filled.Delete,
                    label = "Sil",
                    onClick = { showDeleteConfirm = true },
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            }
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
