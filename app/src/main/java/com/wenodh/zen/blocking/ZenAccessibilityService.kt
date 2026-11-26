package com.wenodh.zen.blocking

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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

        val shouldBlock = AppBlockPolicy.shouldBlock(
            packageName = packageName,
            ownPackageName = this.packageName,
            blockedPackages = BlockedAppsStore(this).blockedPackages(),
            focusSessionActive = FocusSessionStore(this).isActive()
        )
        if (!shouldBlock || packageName == lastBlockedPackage) {
            if (!shouldBlock) lastBlockedPackage = null
            return
        }

        lastBlockedPackage = packageName
        FocusSessionStore(this).recordBlockedAttempt(packageName)
        Log.i(TAG, "Blocked app launch intercepted: $packageName")
        // Accessibility global action returns the user to Home without starting
        // an Activity from the background (Android background-launch limits).
        performGlobalAction(GLOBAL_ACTION_HOME)
        showRestrictionNotification(packageName)
    }

    private fun showRestrictionNotification(packageName: String) {
        runCatching {
            val channelId = "app_restrictions"
            val manager = getSystemService(NotificationManager::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                manager.getNotificationChannel(channelId) == null
            ) {
                manager.createNotificationChannel(
                    NotificationChannel(channelId, "App restrictions", NotificationManager.IMPORTANCE_DEFAULT)
                )
            }

            val intent = Intent(this, BlockedAppActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(BlockedAppActivity.EXTRA_PACKAGE_NAME, packageName)
            val pendingIntent = PendingIntent.getActivity(
                this,
                packageName.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setContentTitle("Focus session active")
                .setContentText("A selected app was blocked. Tap to view.")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
        }.onFailure { Log.w(TAG, "Unable to post restriction notification", it) }
    }

    override fun onInterrupt() {
        lastBlockedPackage = null
    }

    companion object {
        private const val TAG = "ZenAppBlocking"
        private const val NOTIFICATION_ID = 2301
    }
}
