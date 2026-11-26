package com.wenodh.zen.schedule

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class FocusScheduleStore(context: Context) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("focus_schedule", Context.MODE_PRIVATE)

    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)
    fun hour(): Int = prefs.getInt(KEY_HOUR, 9)
    fun minute(): Int = prefs.getInt(KEY_MINUTE, 0)

    fun configure(enabled: Boolean, hour: Int = hour(), minute: Int = minute()) {
        require(hour in 0..23 && minute in 0..59)
        check(prefs.edit().putBoolean(KEY_ENABLED, enabled).putInt(KEY_HOUR, hour).putInt(KEY_MINUTE, minute).commit())
        if (enabled) scheduleNext() else WorkManager.getInstance(app).cancelUniqueWork(UNIQUE_WORK)
    }

    fun scheduleNext(fromWorker: Boolean = false) {
        if (!isEnabled()) return
        val nextTrigger = DailySchedule.nextTriggerMillis(System.currentTimeMillis(), hour(), minute())
        val request = OneTimeWorkRequestBuilder<FocusScheduleWorker>()
            .setInitialDelay(nextTrigger - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(app).enqueueUniqueWork(
            UNIQUE_WORK,
            if (fromWorker) ExistingWorkPolicy.APPEND_OR_REPLACE else ExistingWorkPolicy.REPLACE,
            request
        )
    }

    companion object {
        const val UNIQUE_WORK = "wenodh_zen_daily_focus_reminder"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_HOUR = "hour"
        private const val KEY_MINUTE = "minute"
    }
}
