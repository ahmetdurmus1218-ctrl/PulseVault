package com.screenpulsedev.pulsevault.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

/**
 * Single, unified unlock check: fingerprint/face OR the device's own PIN/pattern/
 * password — whichever the user has set up. This is Android's standard, documented
 * combined-authenticator flow: the system shows ONE dialog, offering biometric first
 * with a native "use PIN instead" fallback baked in by the OS itself. It's the most
 * broadly compatible approach across OEM skins (no custom fallback logic to get wrong).
 */
object BiometricAuthManager {

    private const val ALLOWED_AUTHENTICATORS =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    fun canAuthenticate(activity: FragmentActivity): Boolean {
        val manager = BiometricManager.from(activity)
        return manager.canAuthenticate(ALLOWED_AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        executor: Executor,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // No setNegativeButtonText() here: it's illegal to combine with DEVICE_CREDENTIAL
        // in allowedAuthenticators — the device credential option IS the fallback.
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
                    // A single wrong fingerprint — the OS dialog stays open and lets
                    // the user retry or tap its own PIN/pattern fallback link.
                }
            }
        )

        prompt.authenticate(promptInfo)
    }
}
