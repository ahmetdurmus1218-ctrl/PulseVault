package com.screenpulsedev.pulsevault.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.SystemClock

/**
 * Centralized, single source of truth for all "copy sensitive value to clipboard"
 * behavior in the app (card number, expiry, CVV, IBAN, passwords, etc).
 *
 * Design:
 * - Every copy gets a unique, monotonically increasing token. Only the alarm
 *   carrying the CURRENT latest token is allowed to actually clear the
 *   clipboard — this is what makes rapid sequential copies (number, then
 *   expiry, then CVV) behave correctly instead of an old timer wiping out a
 *   newer copy.
 * - Before clearing, the receiver ALSO double-checks that the clipboard still
 *   holds exactly the text we copied — if the person copied something from a
 *   different app in the meantime, that value is left completely alone.
 * - Uses AlarmManager (setAndAllowWhileIdle), not WorkManager: WorkManager's
 *   delayed one-time work can be deferred by several minutes under Doze/App
 *   Standby, which is exactly the state PulseVault is in right after a copy
 *   (the person immediately switches to another app to paste).
 * - The app going to the background does NOT clear the clipboard early — the
 *   whole point of copying is pasting into another app. Only an explicit
 *   vault lock clears it immediately (see [clearNow]).
 */
object SecureClipboardManager {

    private const val PREFS = "pulsevault_clipboard_prefs"
    private const val KEY_DELAY_SECONDS = "clear_delay_seconds"
    private const val KEY_LATEST_TOKEN = "latest_token"
    private const val EXTRA_TOKEN = "token"
    private const val EXTRA_EXPECTED_TEXT = "expected_text"
    private const val ACTION_CLEAR = "com.screenpulsedev.pulsevault.CLEAR_CLIPBOARD"

    /** Every option the person can choose in Settings, in seconds. */
    val DELAY_OPTIONS = (5..60 step 5).toList()
    const val DEFAULT_DELAY_SECONDS = 30

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getDelaySeconds(context: Context): Int =
        prefs(context).getInt(KEY_DELAY_SECONDS, DEFAULT_DELAY_SECONDS)

    fun setDelaySeconds(context: Context, seconds: Int) {
        prefs(context).edit().putInt(KEY_DELAY_SECONDS, seconds).apply()
    }

    /** Copies [text], marks it sensitive where supported, and schedules its auto-clear. */
    fun copy(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            clip.description.extras = android.os.PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }
        clipboard.setPrimaryClip(clip)

        val token = (prefs(context).getLong(KEY_LATEST_TOKEN, 0L) + 1)
        prefs(context).edit().putLong(KEY_LATEST_TOKEN, token).apply()

        val delaySeconds = getDelaySeconds(context)
        val intent = Intent(context, ClipboardClearReceiver::class.java).apply {
            action = ACTION_CLEAR
            putExtra(EXTRA_TOKEN, token)
            putExtra(EXTRA_EXPECTED_TEXT, text)
        }
        // requestCode must stay constant so a fresh copy's PendingIntent replaces
        // (not stacks alongside) any still-pending alarm from a previous copy.
        val pendingIntent = PendingIntent.getBroadcast(
            context, 1001, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = SystemClock.elapsedRealtime() + delaySeconds * 1000L
        alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent)
    }

    /** Called when the vault locks — wipes anything PulseVault copied, right now. */
    fun clearNow(context: Context) {
        // Bump the token so any still-pending alarm becomes stale and no-ops
        // when it eventually fires (it'll see its captured token no longer matches).
        val newToken = (prefs(context).getLong(KEY_LATEST_TOKEN, 0L) + 1)
        prefs(context).edit().putLong(KEY_LATEST_TOKEN, newToken).apply()

        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // Only clear if there's actually something there — avoids pointlessly
            // touching the clipboard (and thus other apps' clipboard-changed
            // listeners) when PulseVault never copied anything this session.
            if (!clipboard.primaryClip?.getItemAt(0)?.text.isNullOrEmpty()) {
                clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
            }
        } catch (_: Exception) {
            // Best-effort.
        }
    }

    internal fun isTokenStillCurrent(context: Context, token: Long): Boolean =
        prefs(context).getLong(KEY_LATEST_TOKEN, 0L) == token
}

class ClipboardClearReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val token = intent.getLongExtra("token", -1L)
        val expected = intent.getStringExtra("expected_text") ?: return

        // Stale alarm from an older copy that's since been superseded — ignore.
        if (!SecureClipboardManager.isTokenStillCurrent(context, token)) return

        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val current = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
            // Only clear if the clipboard still holds exactly what we copied —
            // if the person copied something else since, leave it untouched.
            if (current == expected) {
                clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
            }
        } catch (_: Exception) {
            // Best-effort.
        }
    }
}

/** Kept for source compatibility with existing call sites. */
fun copySensitiveText(context: Context, label: String, text: String) =
    SecureClipboardManager.copy(context, label, text)
