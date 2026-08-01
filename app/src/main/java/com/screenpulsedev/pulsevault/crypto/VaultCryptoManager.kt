package com.screenpulsedev.pulsevault.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Handles all encryption/decryption for vault contents.
 *
 * Design:
 * - The AES-256 key is generated INSIDE the Android Keystore (hardware-backed on most
 *   devices: TEE, or StrongBox on flagship phones). The raw key bytes never exist in
 *   app memory or on disk — only the Keystore can use it.
 * - setUserAuthenticationRequired(true) means the key is *unusable* until the user
 *   passes a biometric (or device credential) check. This is enforced by the OS/hardware,
 *   not by app logic, so it can't be bypassed by patching the APK.
 * - Every encrypt call uses a fresh random IV (GCM requirement), stored alongside the
 *   ciphertext since it isn't secret.
 */
object VaultCryptoManager {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "pulsevault_master_key_v3"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val AUTH_VALID_SECONDS = 30 // key usable for 30s after a successful unlock

    private fun keyStore(): KeyStore =
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    /** Creates the hardware-backed key on first run. Safe to call repeatedly. */
    fun ensureKeyExists() {
        val ks = keyStore()
        if (ks.containsAlias(KEY_ALIAS)) return

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE
        )
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            // Time-bound key (valid for AUTH_VALID_SECONDS after a fresh unlock).
            // This is what lets device PIN/pattern work as a fallback, not just
            // fingerprint/face — device-credential unlock can't be bound directly
            // to a single Cipher via CryptoObject the way biometric-only can, so
            // the app authenticates generically first (see BiometricAuthManager),
            // then creates+uses the cipher inside that validity window.
            .setUserAuthenticationParameters(
                AUTH_VALID_SECONDS,
                KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL
            )
            .setInvalidatedByBiometricEnrollment(true) // new fingerprint enrolled -> key dies
            .build()

        keyGenerator.init(spec)
        keyGenerator.generateKey()
    }

    private fun secretKey(): SecretKey =
        keyStore().getKey(KEY_ALIAS, null) as SecretKey

    /** Cipher ready for encryption; pass into BiometricPrompt.CryptoObject. */
    fun getEncryptCipher(): Cipher {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        return cipher
    }

    /** Cipher ready for decryption of a specific blob; pass into BiometricPrompt.CryptoObject. */
    fun getDecryptCipher(iv: ByteArray): Cipher {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), spec)
        return cipher
    }

    /** Call only with a cipher that biometric auth has already unlocked. */
    fun encrypt(cipher: Cipher, plaintext: String): EncryptedBlob {
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return EncryptedBlob(ciphertext = ciphertext, iv = cipher.iv)
    }

    fun decrypt(cipher: Cipher, blob: EncryptedBlob): String {
        val plaintext = cipher.doFinal(blob.ciphertext)
        return String(plaintext, Charsets.UTF_8)
    }
}

data class EncryptedBlob(
    val ciphertext: ByteArray,
    val iv: ByteArray
)
