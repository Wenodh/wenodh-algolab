package com.wenodh.zen.blocking

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.wenodh.zen.focus.FocusSessionStore

/**
 * Observes foreground package transitions only. It does not read window text,
 * view hierarchies, keystrokes, notifications, or transmit device activity.
 */
class ZenAccessibilityService : AccessibilityService() {
    private var lastBlockedPackage: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        if (packageName == this.packageName) {
            lastBlockedPackage = null
            return
        }

        if (!FocusSessionStore(this).isActive()) {
            lastBlockedPackage = null
            return
        }

        val blocked = BlockedAppsStore(this).blockedPackages()
        if (packageName !in blocked || packageName == lastBlockedPackage) {
            if (packageName !in blocked) lastBlockedPackage = null
            return
        }

        lastBlockedPackage = packageName
        Log.i(TAG, "Blocked app launch intercepted: $packageName")
        // Move away from the restricted app before presenting the explanation.
        performGlobalAction(GLOBAL_ACTION_HOME)
        runCatching {
            startActivity(
                Intent(this, BlockedAppActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .putExtra(BlockedAppActivity.EXTRA_PACKAGE_NAME, packageName)
            )
        }.onFailure { Log.e(TAG, "Unable to show restriction screen", it) }
    }

    override fun onInterrupt() {
        lastBlockedPackage = null
    }

    companion object {
        private const val TAG = "ZenAppBlocking"
    }
}
