package com.screenpulsedev.pulsevault.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

            // Card number: digits only, grouped in 4s, capped at 19 digits (covers all
            // major card networks — Amex is 15, most others 16, some debit up to 19).
            OutlinedTextField(
                value = formatCardNumber(number),
                onValueChange = { input -> number = input.filter { it.isDigit() }.take(19) },
                label = { Text("Kart Numarası / IBAN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // Expiry: digits only, auto-inserts "/" after MM, capped at MM/YY (4 digits).
            OutlinedTextField(
                value = formatExpiry(expiry),
                onValueChange = { input -> expiry = input.filter { it.isDigit() }.take(4) },
                label = { Text("Son Kullanma (AA/YY)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // CVV: digits only, capped at 4 (Amex uses 4, everyone else 3), masked like a PIN.
            OutlinedTextField(
                value = cvv,
                onValueChange = { input -> cvv = input.filter { it.isDigit() }.take(4) },
                label = { Text("CVV") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Not (opsiyonel)") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            val (saveScale, saveInteraction) = com.screenpulsedev.pulsevault.ui.theme.rememberPressScale()
            Button(
                onClick = {
                    onSave(
                        label.ifBlank { "Adsız Kayıt" },
                        VaultCategory.CREDIT_CARD,
                        VaultItemPayload(holderName, number, expiry, cvv, notes)
                    )
                },
                interactionSource = saveInteraction,
                modifier = saveScale.fillMaxWidth()
            ) { Text("Şifrele ve Kaydet") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Vazgeç") }
        }
    }
}

/** "4111111111111111" -> "4111 1111 1111 1111" for on-screen readability. */
private fun formatCardNumber(digits: String): String =
    digits.chunked(4).joinToString(" ")

/** "1226" -> "12/26" for on-screen readability. */
private fun formatExpiry(digits: String): String =
    if (digits.length <= 2) digits else "${digits.take(2)}/${digits.drop(2)}"
