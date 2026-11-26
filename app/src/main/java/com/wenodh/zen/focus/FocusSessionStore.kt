package com.wenodh.zen.focus

import android.content.Context

/**
 * Small durable session store. The end timestamp is the source of truth;
 * the UI never relies on a timer loop surviving process death.
 */
class FocusSessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("focus_session", Context.MODE_PRIVATE)

    fun start(durationMinutes: Int) {
        val now = System.currentTimeMillis()
        prefs.edit()
            .putLong(KEY_STARTED_AT, now)
            .putLong(KEY_ENDS_AT, now + durationMinutes * 60_000L)
            .putInt(KEY_DURATION, durationMinutes)
            .putBoolean(KEY_ACTIVE, true)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun isActive(): Boolean = prefs.getBoolean(KEY_ACTIVE, false) && remainingMillis() > 0L

    fun remainingMillis(): Long = (prefs.getLong(KEY_ENDS_AT, 0L) - System.currentTimeMillis()).coerceAtLeast(0L)

    fun durationMinutes(): Int = prefs.getInt(KEY_DURATION, 25)

    fun finishIfExpired(): Boolean {
        if (prefs.getBoolean(KEY_ACTIVE, false) && remainingMillis() == 0L) {
            clear()
            return true
        }
        return false
    }

    companion object {
        private const val KEY_STARTED_AT = "started_at"
        private const val KEY_ENDS_AT = "ends_at"
        private const val KEY_DURATION = "duration_minutes"
        private const val KEY_ACTIVE = "active"
    }
}
