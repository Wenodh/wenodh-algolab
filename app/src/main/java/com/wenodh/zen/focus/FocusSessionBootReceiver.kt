package com.wenodh.zen.focus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/** Reattaches the ongoing user-started timer after device reboot. */
class FocusSessionBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val store = FocusSessionStore(context)
        if (!store.hasPendingSession()) return

        // The deadline remains persisted if Android/OEM policy rejects restart.
        runCatching { FocusSessionService.start(context) }
            .onFailure { Log.w(TAG, "Could not resume focus service after reboot", it) }
    }

    companion object {
        private const val TAG = "ZenBootReceiver"
    }
}
