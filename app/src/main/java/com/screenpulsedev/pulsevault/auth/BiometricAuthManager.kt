package com.screenpulsedev.pulsevault.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor
import javax.crypto.Cipher

/**
 * Wraps BiometricPrompt so every unlock/encrypt/decrypt is tied to a live
 * biometric (or device PIN/pattern fallback) check via a CryptoObject.
 *
 * Because the Cipher passed in comes from VaultCryptoManager (a Keystore key with
 * setUserAuthenticationRequired(true)), the OS refuses to run the cipher unless this
 * prompt has just succeeded. There is no code path that decrypts data without it.
 */
object BiometricAuthManager {

    /** BIOMETRIC_STRONG (fingerprint/face) with PIN/pattern/password as fallback. */
    private const val ALLOWED_AUTHENTICATORS =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    fun canAuthenticate(activity: FragmentActivity): Boolean {
        val manager = BiometricManager.from(activity)
        return manager.canAuthenticate(ALLOWED_AUTHENTICATORS) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        cipher: Cipher,
        title: String,
        subtitle: String,
        executor: Executor,
        onSuccess: (Cipher) -> Unit,
        onError: (String) -> Unit
    ) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .build()

        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val authedCipher = result.cryptoObject?.cipher
                    if (authedCipher != null) {
                        onSuccess(authedCipher)
                    } else {
                        onError("Şifreleme nesnesi alınamadı")
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    // A single failed attempt (e.g. wrong finger) — prompt stays open, no-op.
                }
            }
        )

        prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }
}
