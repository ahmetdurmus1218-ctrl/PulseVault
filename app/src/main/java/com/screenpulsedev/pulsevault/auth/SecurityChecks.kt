package com.screenpulsedev.pulsevault.auth

import android.content.Context
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityServiceInfo

/**
 * Checks whether any accessibility service is currently active on the device.
 * This can't distinguish a legitimate one (TalkBack, Voice Access) from a
 * malicious screen-reading trojan — Android doesn't expose that distinction to
 * apps. So this is intentionally just an informational heads-up, not a block:
 * "something can read what's on your screen right now, make sure you know what
 * it is" — the same approach several banking apps take.
 */
fun hasActiveAccessibilityServices(context: Context): Boolean {
    val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        ?: return false
    val enabled = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    return enabled.isNotEmpty()
}
