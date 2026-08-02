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
 */
object PinManager {
    private const val PREFS = "pulsevault_pin_prefs"
    private const val KEY_HASH = "pin_hash"
    private const val KEY_SALT = "pin_salt"

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
            .apply()
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        val saltHex = prefs(context).getString(KEY_SALT, null) ?: return false
        val storedHash = prefs(context).getString(KEY_HASH, null) ?: return false
        val salt = saltHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        return hash(pin, salt) == storedHash
    }

    fun clearPin(context: Context) {
        prefs(context).edit().remove(KEY_HASH).remove(KEY_SALT).apply()
    }

    private fun hash(pin: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val bytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
