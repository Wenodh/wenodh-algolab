package com.wenodh.zen.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FocusScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED)) {
            FocusScheduleStore(context).scheduleNext()
        }
    }
}
