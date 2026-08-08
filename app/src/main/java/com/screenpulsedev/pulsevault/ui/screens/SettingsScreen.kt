package com.screenpulsedev.pulsevault.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.screenpulsedev.pulsevault.util.SecureClipboardManager

@Composable
fun SettingsScreen(
    isAppPinEnabled: Boolean,
    onToggleAppPin: (enable: Boolean) -> Unit,
    clipboardDelaySeconds: Int,
    onClipboardDelayChange: (Int) -> Unit,
    onRequestBatteryExemption: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Ek PIN Katmanı", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Cihaz kilidinden sonra, uygulamaya özel 6 haneli bir PIN daha iste (art arda hatalı denemede geçici kilitlenir)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = isAppPinEnabled, onCheckedChange = onToggleAppPin)
            }

            Spacer(Modifier.height(24.dp))

            Text("Hassas Kopyaları Otomatik Temizleme Süresi", fontWeight = FontWeight.SemiBold)
            Text(
                "Kart numarası, CVV, IBAN gibi kopyaladığın bilgiler bu süre sonunda panodan otomatik silinir",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SecureClipboardManager.DELAY_OPTIONS) { seconds ->
                    FilterChip(
                        selected = clipboardDelaySeconds == seconds,
                        onClick = { onClipboardDelayChange(seconds) },
                        label = { Text("${seconds}sn") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            Text("Güvenilir Arka Plan Temizleme", fontWeight = FontWeight.SemiBold)
            Text(
                "Bazı telefonlar (özellikle Vivo, Xiaomi, Oppo) pil tasarrufu için arka plan " +
                    "zamanlayıcılarını durdurabiliyor — bu da pano temizlemenin gecikmesine neden olur. " +
                    "PulseVault'u pil optimizasyonundan hariç tutmak bunu çözer.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.Button(
                onClick = onRequestBatteryExemption,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pil Optimizasyonundan Hariç Tut")
            }
        }
    }
}
