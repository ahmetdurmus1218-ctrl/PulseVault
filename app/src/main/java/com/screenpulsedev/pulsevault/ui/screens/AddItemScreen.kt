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
import androidx.compose.foundation.horizontalScroll
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
    screenTitle: String = "Yeni Kayıt",
    initialLabel: String = "",
    initialCategory: VaultCategory = VaultCategory.CREDIT_CARD,
    initialBank: String = "",
    initialNetwork: CardNetwork? = null,
    initialIsVirtual: Boolean = false,
    initialPayload: VaultItemPayload = VaultItemPayload(),
    lockCategory: Boolean = false,
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

    var category by remember { mutableStateOf(initialCategory) }
    var label by remember { mutableStateOf(initialLabel) }
    var holderName by remember { mutableStateOf(initialPayload.holderName) }
    var number by remember { mutableStateOf(initialPayload.number) }
    var expiry by remember { mutableStateOf(initialPayload.expiry.filter { it.isDigit() }) }
    var cvv by remember { mutableStateOf(initialPayload.cvv) }
    var notes by remember { mutableStateOf(initialPayload.notes) }
    var username by remember { mutableStateOf(initialPayload.username) }
    var password by remember { mutableStateOf(initialPayload.password) }
    var bank by remember { mutableStateOf(initialBank) }
    var network by remember { mutableStateOf(initialNetwork) }
    var isVirtual by remember { mutableStateOf(initialIsVirtual) }

    val cardDigitsOnly = number.filter { it.isDigit() }
    val detectedNetwork = network ?: CardNetwork.fromCardNumber(cardDigitsOnly)
    val isCardLike = category == VaultCategory.CREDIT_CARD || category == VaultCategory.BANK_ACCOUNT

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
            if (!lockCategory) {
                Text("Tür", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    listOf(
                        VaultCategory.CREDIT_CARD to "Kart",
                        VaultCategory.BANK_ACCOUNT to "Hesap",
                        VaultCategory.PASSWORD to "Şifre",
                        VaultCategory.NOTE to "Not"
                    ).forEach { (cat, catLabel) ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(catLabel) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            if (isCardLike) {
                CreditCardView(
                    label = label.ifBlank { "Yeni Kart" },
                    category = category,
                    network = detectedNetwork,
                    lastFourDigits = cardDigitsOnly.takeLast(4),
                    bank = bank,
                    isVirtual = isVirtual
                )
                Spacer(Modifier.height(20.dp))
            }

            OutlinedTextField(
                value = label, onValueChange = { label = it },
                label = { Text(labelFieldTitle(category)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            if (isCardLike) {
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
            }

            if (category == VaultCategory.CREDIT_CARD) {
                Text("Kart Ağı", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
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
            }

            when (category) {
                VaultCategory.CREDIT_CARD -> {
                    OutlinedTextField(
                        value = holderName, onValueChange = { holderName = it },
                        label = { Text("Kart Sahibi") }, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = number,
                        onValueChange = { input -> number = input.filter { it.isDigit() }.take(19) },
                        label = { Text("Kart Numarası") },
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
                }

                VaultCategory.BANK_ACCOUNT -> {
                    OutlinedTextField(
                        value = holderName, onValueChange = { holderName = it },
                        label = { Text("Hesap Sahibi") }, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = number, onValueChange = { number = it.uppercase() },
                        label = { Text("IBAN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                VaultCategory.PASSWORD -> {
                    OutlinedTextField(
                        value = username, onValueChange = { username = it },
                        label = { Text("Kullanıcı Adı / E-posta") }, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        label = { Text("Şifre") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                VaultCategory.NOTE -> {
                    OutlinedTextField(
                        value = notes, onValueChange = { notes = it },
                        label = { Text("Not İçeriği") },
                        minLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (category != VaultCategory.NOTE) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Not (opsiyonel)") }, modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    onSave(
                        label.ifBlank { "Adsız Kayıt" },
                        category,
                        VaultItemPayload(holderName, number, expiry, cvv, notes, username, password),
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

private fun labelFieldTitle(category: VaultCategory): String = when (category) {
    VaultCategory.CREDIT_CARD -> "Etiket (örn. Alışveriş Kartım)"
    VaultCategory.BANK_ACCOUNT -> "Etiket (örn. Maaş Hesabım)"
    VaultCategory.PASSWORD -> "Servis / Site Adı"
    VaultCategory.NOTE -> "Başlık"
}
