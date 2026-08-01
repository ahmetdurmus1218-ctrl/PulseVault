package com.screenpulsedev.pulsevault.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.screenpulsedev.pulsevault.data.VaultCategory
import com.screenpulsedev.pulsevault.data.VaultItemPayload

@Composable
fun AddItemScreen(
    onSave: (label: String, category: VaultCategory, payload: VaultItemPayload) -> Unit,
    onCancel: () -> Unit
) {
    var label by remember { mutableStateOf("") }
    var holderName by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Yeni Kayıt") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = label, onValueChange = { label = it },
                label = { Text("Etiket (örn. İş Bankası Kredi Kartı)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = holderName, onValueChange = { holderName = it },
                label = { Text("Kart Sahibi") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = number, onValueChange = { number = it },
                label = { Text("Kart Numarası / IBAN") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = expiry, onValueChange = { expiry = it },
                label = { Text("Son Kullanma (AA/YY)") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = cvv, onValueChange = { cvv = it },
                label = { Text("CVV") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Not (opsiyonel)") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    onSave(
                        label.ifBlank { "Adsız Kayıt" },
                        VaultCategory.CREDIT_CARD,
                        VaultItemPayload(holderName, number, expiry, cvv, notes)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Şifrele ve Kaydet") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Vazgeç") }
        }
    }
}
