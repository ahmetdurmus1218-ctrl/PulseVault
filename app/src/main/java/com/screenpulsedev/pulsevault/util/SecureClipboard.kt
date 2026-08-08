package com.screenpulsedev.pulsevault.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock

private const val CLEAR_DELAY_MILLIS = 15_000L
private const val KEY_EXPECTED_TEXT = "expected_text"
private const val ACTION_CLEAR_CLIPBOARD = "com.screenpulsedev.pulsevault.CLEAR_CLIPBOARD"

/**
 * Copies sensitive text (card number, CVV, etc.) to the clipboard, marks it
 * EXTRA_IS_SENSITIVE (Android 13+ then hides it from clipboard history / previews),
 * and schedules an automatic wipe 15 seconds later.
 *
 * This uses AlarmManager, NOT WorkManager. WorkManager's delayed one-time work is
 * only best-effort and can be pushed back by several minutes under Doze/App
 * Standby — exactly the situation right after copying, since the person
 * immediately switches to another app to paste, backgrounding PulseVault.
 * AlarmManager's setAndAllowWhileIdle is designed to still fire close to the
 * requested time even while the device is idle.
 */
fun copySensitiveText(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        clip.description.extras = android.os.PersistableBundle().apply {
            putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
        }
    }
    clipboard.setPrimaryClip(clip)

    val intent = Intent(context, ClipboardClearReceiver::class.java).apply {
        action = ACTION_CLEAR_CLIPBOARD
        putExtra(KEY_EXPECTED_TEXT, text)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val triggerAt = SystemClock.elapsedRealtime() + CLEAR_DELAY_MILLIS
    alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent)
}

/** Only clears the clipboard if it still holds the value we copied (avoids wiping something newer). */
class ClipboardClearReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_CLEAR_CLIPBOARD) return
        val expected = intent.getStringExtra(KEY_EXPECTED_TEXT) ?: return

        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val current = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
            if (current == expected) {
                clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
            }
        } catch (_: Exception) {
            // Best-effort — nothing more we can do if this fails.
        }
    }
}
