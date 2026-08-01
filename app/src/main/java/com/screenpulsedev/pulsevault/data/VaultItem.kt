package com.screenpulsedev.pulsevault.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Every sensitive field is stored as ciphertext + IV, never as plaintext.
 * "label" (e.g. "İş Bankası Kredi Kartı") stays unencrypted since it's just a
 * display name with no sensitive value on its own — this lets the list screen
 * render without unlocking the vault first.
 */
@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val category: VaultCategory,

    // Encrypted payload (card number, CVV, expiry, holder name, notes — JSON-encoded then encrypted)
    val encryptedData: ByteArray,
    val iv: ByteArray,

    val createdAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VaultItem) return false
        return id == other.id && label == other.label && category == other.category &&
            encryptedData.contentEquals(other.encryptedData) && iv.contentEquals(other.iv)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + encryptedData.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}

enum class VaultCategory {
    CREDIT_CARD, BANK_ACCOUNT, PASSWORD, NOTE
}

/** Plaintext shape used only in memory, right after decryption — never persisted as-is. */
data class VaultItemPayload(
    val holderName: String = "",
    val number: String = "",       // card number / IBAN
    val expiry: String = "",       // MM/YY
    val cvv: String = "",
    val notes: String = ""
)
