package com.screenpulsedev.pulsevault.auth

import android.content.Context
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * An OPTIONAL extra gate on top of the device biometric/PIN check: a short PIN
 * specific to this app. This does NOT replace the Keystore-backed encryption —
 * that's still gated by the real device auth. This is only a second "someone
 * picked up my already-unlocked phone" speed bump, so simple salted-hash storage
 * (not a Keystore-backed secret) is an appropriate, simple fit for its purpose.
 *
 * 6 digits (1,000,000 combinations, not 10,000) + exponential lockout after
 * repeated failures makes brute-forcing this PIN impractical even with
 * unlimited local attempts.
 */
object PinManager {
    private const val PREFS = "pulsevault_pin_prefs"
    private const val KEY_HASH = "pin_hash"
    private const val KEY_SALT = "pin_salt"
    private const val KEY_FAIL_COUNT = "pin_fail_count"
    private const val KEY_LOCKOUT_UNTIL = "pin_lockout_until"

    const val PIN_LENGTH = 6

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isPinSet(context: Context): Boolean =
        prefs(context).contains(KEY_HASH)

    fun setPin(context: Context, pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = hash(pin, salt)
        prefs(context).edit()
            .putString(KEY_SALT, salt.joinToString("") { "%02x".format(it) })
            .putString(KEY_HASH, hash)
            .putInt(KEY_FAIL_COUNT, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()
    }

    /** Seconds remaining before another attempt is allowed, or 0 if not locked out. */
    fun lockoutSecondsRemaining(context: Context): Long {
        val until = prefs(context).getLong(KEY_LOCKOUT_UNTIL, 0L)
        val remaining = (until - System.currentTimeMillis()) / 1000
        return remaining.coerceAtLeast(0)
    }

    /** Returns null if locked out (check [lockoutSecondsRemaining] first), else true/false for the PIN. */
    fun verifyPin(context: Context, pin: String): Boolean {
        if (lockoutSecondsRemaining(context) > 0) return false

        val saltHex = prefs(context).getString(KEY_SALT, null) ?: return false
        val storedHash = prefs(context).getString(KEY_HASH, null) ?: return false
        val salt = saltHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val correct = hash(pin, salt) == storedHash

        val editor = prefs(context).edit()
        if (correct) {
            editor.putInt(KEY_FAIL_COUNT, 0).putLong(KEY_LOCKOUT_UNTIL, 0L)
        } else {
            val fails = prefs(context).getInt(KEY_FAIL_COUNT, 0) + 1
            editor.putInt(KEY_FAIL_COUNT, fails)
            // Exponential backoff: 3 fails -> 30s, 5 -> 2min, 7 -> 10min, 9+ -> 30min.
            val lockoutSeconds = when {
                fails >= 9 -> 1800L
                fails >= 7 -> 600L
                fails >= 5 -> 120L
                fails >= 3 -> 30L
                else -> 0L
            }
            if (lockoutSeconds > 0) {
                editor.putLong(KEY_LOCKOUT_UNTIL, System.currentTimeMillis() + lockoutSeconds * 1000)
            }
        }
        editor.apply()
        return correct
    }

    fun clearPin(context: Context) {
        prefs(context).edit()
            .remove(KEY_HASH).remove(KEY_SALT)
            .remove(KEY_FAIL_COUNT).remove(KEY_LOCKOUT_UNTIL)
            .apply()
    }

    private fun hash(pin: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val bytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
