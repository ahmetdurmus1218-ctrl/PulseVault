package com.screenpulsedev.pulsevault.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.screenpulsedev.pulsevault.data.CardNetwork
import com.screenpulsedev.pulsevault.data.TURKISH_BANKS
import com.screenpulsedev.pulsevault.data.VaultCategory
import com.screenpulsedev.pulsevault.data.VaultItemPayload
import com.screenpulsedev.pulsevault.ui.components.CardNumberVisualTransformation
import com.screenpulsedev.pulsevault.ui.components.CreditCardView
import com.screenpulsedev.pulsevault.ui.components.ExpiryVisualTransformation

@Composable
fun AddItemScreen(
    screenTitle: String = "Yeni Kart",
    initialLabel: String = "",
    initialBank: String = "",
    initialNetwork: CardNetwork? = null,
    initialIsVirtual: Boolean = false,
    initialPayload: VaultItemPayload = VaultItemPayload(),
    onSave: (
        label: String,
        category: VaultCategory,
        payload: VaultItemPayload,
        network: CardNetwork,
        bank: String,
        isVirtual: Boolean
    ) -> Unit,
    onCancel: () -> Unit
) {
    BackHandler(onBack = onCancel)

    var label by remember { mutableStateOf(initialLabel) }
    var holderName by remember { mutableStateOf(initialPayload.holderName) }
    var number by remember { mutableStateOf(initialPayload.number.filter { it.isDigit() }) }
    var expiry by remember { mutableStateOf(initialPayload.expiry.filter { it.isDigit() }) }
    var cvv by remember { mutableStateOf(initialPayload.cvv) }
    var notes by remember { mutableStateOf(initialPayload.notes) }
    var bank by remember { mutableStateOf(initialBank) }
    var network by remember { mutableStateOf(initialNetwork) }
    var isVirtual by remember { mutableStateOf(initialIsVirtual) }

    val detectedNetwork = network ?: CardNetwork.fromCardNumber(number)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            CreditCardView(
                label = label.ifBlank { "Yeni Kart" },
                category = VaultCategory.CREDIT_CARD,
                network = detectedNetwork,
                lastFourDigits = number.takeLast(4),
                bank = bank,
                isVirtual = isVirtual
            )
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = label, onValueChange = { label = it },
                label = { Text("Etiket (örn. Alışveriş Kartım)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Text("Banka", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(TURKISH_BANKS) { bankName ->
                    FilterChip(
                        selected = bank == bankName,
                        onClick = { bank = if (bank == bankName) "" else bankName },
                        label = { Text(bankName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            Text("Kart Ağı", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    CardNetwork.TROY to "Troy",
                    CardNetwork.VISA to "Visa",
                    CardNetwork.MASTERCARD to "Mastercard",
                    CardNetwork.AMEX to "Amex"
                ).forEach { (net, netLabel) ->
                    FilterChip(
                        selected = detectedNetwork == net,
                        onClick = { network = net },
                        label = { Text(netLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sanal Kart", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Switch(checked = isVirtual, onCheckedChange = { isVirtual = it })
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = holderName, onValueChange = { holderName = it },
                label = { Text("Kart Sahibi") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // Raw digits stay as the actual state; VisualTransformation only changes
            // what's drawn on screen, so the cursor never jumps while typing/deleting.
            OutlinedTextField(
                value = number,
                onValueChange = { input -> number = input.filter { it.isDigit() }.take(19) },
                label = { Text("Kart Numarası / IBAN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = CardNumberVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = expiry,
                    onValueChange = { input -> expiry = input.filter { it.isDigit() }.take(4) },
                    label = { Text("Son Kullanma (AA/YY)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ExpiryVisualTransformation(),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = cvv,
                    onValueChange = { input -> cvv = input.filter { it.isDigit() }.take(4) },
                    label = { Text("CVV") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.weight(1f)
                )
            }
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
                        VaultItemPayload(holderName, number, expiry, cvv, notes),
                        detectedNetwork,
                        bank,
                        isVirtual
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Şifrele ve Kaydet") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Vazgeç") }
            Spacer(Modifier.height(16.dp))
        }
    }
}
