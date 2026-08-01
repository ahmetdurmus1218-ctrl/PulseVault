package com.screenpulsedev.pulsevault.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

/**
 * Wraps BiometricPrompt for a *generic* unlock check: fingerprint/face OR the
 * device's own PIN/pattern/password, whichever the user has set up and chooses.
 *
 * This intentionally does NOT bind a Cipher via CryptoObject. Android doesn't
 * support binding device-credential (PIN/pattern) unlocks to a single crypto
 * operation the way it supports binding a fingerprint scan — only BIOMETRIC_STRONG
 * can be tied to a CryptoObject that way. So instead: authenticate generically first,
 * then (within the key's short validity window — see VaultCryptoManager) create and
 * use the Cipher. The Keystore itself still enforces that no encrypt/decrypt can
 * happen without a recent successful unlock; the app can't bypass that.
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
        title: String,
        subtitle: String,
        executor: Executor,
        onSuccess: () -> Unit,
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
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    // A single failed attempt (e.g. wrong finger) — prompt stays open, no-op.
                }
            }
        )

        prompt.authenticate(promptInfo)
    }
}

