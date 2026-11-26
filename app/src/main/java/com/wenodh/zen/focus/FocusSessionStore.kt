package com.wenodh.zen.focus

import android.content.Context
import java.util.Calendar
import java.util.UUID

/**
 * Durable active-session state plus a local history ledger.
 * The persisted deadline is the source of truth across process death/reboot.
 */
class FocusSessionStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("focus_session", Context.MODE_PRIVATE)
    private val history = FocusSessionHistoryDb(appContext)

    @Synchronized
    fun start(durationMinutes: Int) {
        require(durationMinutes in 1..180) { "Duration must be between 1 and 180 minutes." }
        finishIfExpired()
        interrupt()
        val startedAt = System.currentTimeMillis()
        val endsAt = startedAt + durationMinutes * 60_000L
        val id = UUID.randomUUID().toString()
        history.insertSession(id, startedAt, endsAt, durationMinutes)
        check(
            prefs.edit()
                .putString(KEY_SESSION_ID, id)
                .putLong(KEY_STARTED_AT, startedAt)
                .putLong(KEY_ENDS_AT, endsAt)
                .putInt(KEY_DURATION, durationMinutes)
                .putBoolean(KEY_ACTIVE, true)
                .commit()
        ) { "Could not persist focus session." }
    }

    @Synchronized
    fun interrupt(): Boolean {
        if (!hasPendingSession()) return false
        history.finishSession(
            prefs.getString(KEY_SESSION_ID, "").orEmpty(),
            SessionTimeline.STATUS_INTERRUPTED,
            System.currentTimeMillis()
        )
        clearActiveState()
        return true
    }

    fun hasPendingSession(): Boolean = prefs.getBoolean(KEY_ACTIVE, false)

    fun isActive(): Boolean =
        hasPendingSession() && !SessionTimeline.isExpired(endsAtMillis(), System.currentTimeMillis())

    fun remainingMillis(): Long =
        SessionTimeline.remainingMillis(endsAtMillis(), System.currentTimeMillis())

    fun durationMinutes(): Int = prefs.getInt(KEY_DURATION, 25)

    fun startedAtMillis(): Long = prefs.getLong(KEY_STARTED_AT, 0L)

    @Synchronized
    fun finishIfExpired(): Boolean {
        if (!hasPendingSession() || !SessionTimeline.isExpired(endsAtMillis(), System.currentTimeMillis())) {
            return false
        }
        history.finishSession(
            prefs.getString(KEY_SESSION_ID, "").orEmpty(),
            SessionTimeline.STATUS_COMPLETED,
            System.currentTimeMillis()
        )
        clearActiveState()
        return true
    }

    fun todayCompletedMinutes(): Int = history.todayCompletedMinutes(startOfToday())
    fun todayCompletedCount(): Int = history.todayCompletedCount(startOfToday())

    private fun endsAtMillis(): Long = prefs.getLong(KEY_ENDS_AT, 0L)

    private fun clearActiveState() {
        prefs.edit().clear().commit()
    }

    private fun startOfToday(): Long = Calendar.getInstance().run {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        timeInMillis
    }

    companion object {
        private const val KEY_SESSION_ID = "session_id"
        private const val KEY_STARTED_AT = "started_at"
        private const val KEY_ENDS_AT = "ends_at"
        private const val KEY_DURATION = "duration_minutes"
        private const val KEY_ACTIVE = "active"
    }
}
