package com.screenpulsedev.pulsevault.data

import android.content.Context
import com.screenpulsedev.pulsevault.crypto.EncryptedBlob
import com.screenpulsedev.pulsevault.crypto.VaultCryptoManager
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject
import javax.crypto.Cipher

class VaultRepository(context: Context) {

    private val dao = VaultDatabase.getInstance(context).vaultDao()

    fun observeAll(): Flow<List<VaultItem>> = dao.observeAll()

    /** cipher must come from a successful BiometricAuthManager.authenticate() callback. */
    suspend fun addItem(
        cipher: Cipher,
        label: String,
        category: VaultCategory,
        payload: VaultItemPayload,
        network: CardNetwork,
        bank: String,
        isVirtual: Boolean
    ) {
        val json = payloadToJson(payload)
        val blob = VaultCryptoManager.encrypt(cipher, json)
        val digitsOnly = payload.number.filter { it.isDigit() }
        dao.insert(
            VaultItem(
                label = label,
                category = category,
                network = network,
                lastFourDigits = digitsOnly.takeLast(4),
                bank = bank,
                isVirtual = isVirtual,
                encryptedData = blob.ciphertext,
                iv = blob.iv
            )
        )
    }

    /** cipher must be initialized with getDecryptCipher(item.iv) and authenticated. */
    suspend fun decryptItem(cipher: Cipher, item: VaultItem): VaultItemPayload {
        val json = VaultCryptoManager.decrypt(
            cipher,
            EncryptedBlob(ciphertext = item.encryptedData, iv = item.iv)
        )
        return jsonToPayload(json)
    }

    suspend fun deleteItem(item: VaultItem) = dao.delete(item)

    private fun payloadToJson(p: VaultItemPayload): String =
        JSONObject().apply {
            put("holderName", p.holderName)
            put("number", p.number)
            put("expiry", p.expiry)
            put("cvv", p.cvv)
            put("notes", p.notes)
        }.toString()

    private fun jsonToPayload(json: String): VaultItemPayload {
        val obj = JSONObject(json)
        return VaultItemPayload(
            holderName = obj.optString("holderName"),
            number = obj.optString("number"),
            expiry = obj.optString("expiry"),
            cvv = obj.optString("cvv"),
            notes = obj.optString("notes")
        )
    }
}
