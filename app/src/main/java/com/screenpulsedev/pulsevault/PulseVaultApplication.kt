package com.screenpulsedev.pulsevault

import android.app.Application
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Temporary diagnostic aid: catches any uncaught crash, writes the full stack
 * trace to a local file, then hands off to the previous handler so the OS still
 * shows its normal "app stopped" behavior. No vault data is ever included here —
 * only exception class/message/stack, which is safe to read off-device.
 */
class PulseVaultApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val entry = "\n===== $timestamp =====\n$sw"
                File(filesDir, "crash_log.txt").appendText(entry)
            } catch (_: Exception) {
                // If logging itself fails, don't block the crash handoff.
            }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }
}
