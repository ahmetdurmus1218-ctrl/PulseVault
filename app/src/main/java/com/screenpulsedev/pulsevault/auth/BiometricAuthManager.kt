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

    /** Combined: whichever the OS offers first (used by add/view flows, unchanged). */
    const val ANY_METHOD =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    /** Forces the fingerprint/face prompt only — no PIN option shown by the OS. */
    const val BIOMETRIC_ONLY = BiometricManager.Authenticators.BIOMETRIC_STRONG

    /** Forces the device PIN/pattern/password confirmation screen only. */
    const val CREDENTIAL_ONLY = BiometricManager.Authenticators.DEVICE_CREDENTIAL

    fun canAuthenticate(activity: FragmentActivity, authenticators: Int = ANY_METHOD): Boolean {
        val manager = BiometricManager.from(activity)
        return manager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        executor: Executor,
        authenticators: Int = ANY_METHOD,
        onSuccess: () -> Unit,
        onFailedAttempt: () -> Unit = {},
        onLockout: () -> Unit = {},
        onError: (String) -> Unit
    ) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)
            .build()

        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    // The OS itself locks fingerprint out (temporarily or permanently)
                    // after too many wrong attempts — that's the real signal to fall
                    // back to the device PIN/pattern, not just our own tap counter.
                    if (errorCode == BiometricPrompt.ERROR_LOCKOUT ||
                        errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT
                    ) {
                        onLockout()
                    } else {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    // A single wrong fingerprint — prompt stays open; let the caller count these.
                    onFailedAttempt()
                }
            }
        )

        prompt.authenticate(promptInfo)
    }
}

