package com.screenpulsedev.pulsevault.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A BLOCKING screen shown right after successful unlock, before any card/account
 * data is ever displayed, if an active accessibility service was detected. The
 * user must make an explicit choice — nothing sensitive renders behind this.
 */
@Composable
fun AccessibilityWarningScreen(
    onOpenSettings: () -> Unit,
    onProceedAnyway: () -> Unit,
    onLock: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "Ekranını Okuyabilecek Bir Servis Aktif",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Cihazında aktif bir erişilebilirlik servisi tespit ettik. Bu, TalkBack gibi " +
                "meşru bir servis olabilir — ama ekrandaki kart bilgilerini okuyabilen kötü " +
                "amaçlı bir uygulama da olabilir. Tanımadığın bir şeyse devam etmeden önce kapat.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Erişilebilirlik Ayarlarını Aç") }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onProceedAnyway,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) { Text("Tanıyorum, Yine de Devam Et") }
        Spacer(Modifier.height(6.dp))
        TextButton(onClick = onLock) { Text("Vazgeç, Kilitle") }
    }
}
