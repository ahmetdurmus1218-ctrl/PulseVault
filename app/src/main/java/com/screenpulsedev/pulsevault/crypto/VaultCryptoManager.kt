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
    // v2: bumped alias so any broken key from the earlier (incorrectly time-bound)
    // config gets regenerated fresh with the correct per-operation setup below.
    private const val KEY_ALIAS = "pulsevault_master_key_v2"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH_BITS = 128

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
            // No setUserAuthenticationParameters() call: this keeps the key in
            // "per-operation" mode, meaning EVERY encrypt/decrypt must be paired
            // with a fresh, successful BiometricPrompt that is bound to that exact
            // Cipher via CryptoObject (which is how BiometricAuthManager uses it).
            // Setting a time-bound duration here was the bug — it made Cipher.init()
            // require a *prior* generic auth event before the biometric prompt even
            // ran, throwing UserNotAuthenticatedException immediately on tap.
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
