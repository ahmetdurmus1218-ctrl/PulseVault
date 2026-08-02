package com.screenpulsedev.pulsevault.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Every sensitive field is stored as ciphertext + IV, never as plaintext.
 * "label" and the two fields below are the only plaintext fields — same tradeoff
 * every real banking app makes: a card network logo and its last 4 digits are not
 * sensitive on their own (they're printed on the physical card, visible to anyone
 * who glances at it), so keeping them in the clear lets the list screen render a
 * realistic card preview without requiring a biometric unlock just to browse names.
 */
@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val category: VaultCategory,
    val network: CardNetwork = CardNetwork.OTHER,
    val lastFourDigits: String = "",
    val bank: String = "",
    val isVirtual: Boolean = false,

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

enum class CardNetwork {
    VISA, MASTERCARD, TROY, AMEX, OTHER;

    companion object {
        /** Best-effort guess from the first digits — TROY has no single public BIN
         * range (issued by many Turkish banks under different prefixes), so this
         * only nails VISA/Mastercard/Amex reliably; TROY/others need the user's
         * own selection in the add-card screen. */
        fun fromCardNumber(digits: String): CardNetwork = when {
            digits.startsWith("4") -> VISA
            digits.startsWith("34") || digits.startsWith("37") -> AMEX
            digits.length >= 2 && digits.substring(0, 2).toIntOrNull()?.let { it in 51..55 } == true -> MASTERCARD
            else -> OTHER
        }
    }
}

/** Common Turkish banks for the picker — plaintext, printed on the card itself. */
val TURKISH_BANKS = listOf(
    "Ziraat Bankası", "İş Bankası", "Garanti BBVA", "Yapı Kredi", "Akbank",
    "QNB Finansbank", "Halkbank", "VakıfBank", "TEB", "DenizBank", "ING", "Diğer"
)

/** Plaintext shape used only in memory, right after decryption — never persisted as-is. */
data class VaultItemPayload(
    val holderName: String = "",
    val number: String = "",       // card number / IBAN
    val expiry: String = "",       // MM/YY
    val cvv: String = "",
    val notes: String = "",        // also used as the body text for NOTE category
    val username: String = "",     // PASSWORD category
    val password: String = ""      // PASSWORD category
)
