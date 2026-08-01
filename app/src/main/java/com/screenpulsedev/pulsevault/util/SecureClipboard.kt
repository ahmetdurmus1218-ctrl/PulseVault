package com.screenpulsedev.pulsevault.util

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

private const val CLEAR_DELAY_SECONDS = 30L
private const val KEY_EXPECTED_TEXT = "expected_text"

/**
 * Copies sensitive text (card number, CVV, etc.) to the clipboard, marks it
 * EXTRA_IS_SENSITIVE (Android 13+ then hides it from clipboard history / previews),
 * and schedules an automatic wipe after 30 seconds so it doesn't linger.
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

    val request = OneTimeWorkRequestBuilder<ClipboardClearWorker>()
        .setInitialDelay(CLEAR_DELAY_SECONDS, TimeUnit.SECONDS)
        .setInputData(workDataOf(KEY_EXPECTED_TEXT to text))
        .build()
    WorkManager.getInstance(context).enqueue(request)
}

/** Only clears the clipboard if it still holds the value we copied (avoids wiping something newer). */
class ClipboardClearWorker(appContext: Context, params: WorkerParameters) :
    Worker(appContext, params) {

    override fun doWork(): Result {
        val expected = inputData.getString(KEY_EXPECTED_TEXT) ?: return Result.success()
        val clipboard = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val current = clipboard.primaryClip?.getItemAt(0)?.text?.toString()

        if (current == expected) {
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
        }
        return Result.success()
    }
}
