package com.wenodh.zen.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FocusScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action in setOf(Intent.ACTION_BOOT_COMPLETED, "android.intent.action.TIME_SET", Intent.ACTION_TIMEZONE_CHANGED)) {
            FocusScheduleStore(context).scheduleNext()
        }
    }
}
