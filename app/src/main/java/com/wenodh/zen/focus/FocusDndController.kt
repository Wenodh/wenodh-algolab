package com.wenodh.zen.focus

import android.app.NotificationManager
import android.content.Context

class FocusDndController(context: Context) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("focus_dnd", Context.MODE_PRIVATE)
    private val manager = app.getSystemService(NotificationManager::class.java)

    fun isEnabledForFocus(): Boolean = prefs.getBoolean(KEY_ENABLED, false)
    fun setEnabledForFocus(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }
    fun hasAccess(): Boolean = manager.isNotificationPolicyAccessGranted

    fun applyForSession() {
        if (!isEnabledForFocus() || !hasAccess() || prefs.getBoolean(KEY_APPLIED, false)) return
        val previous = manager.currentInterruptionFilter
        if (prefs.edit().putInt(KEY_PREVIOUS_FILTER, previous).putBoolean(KEY_APPLIED, true).commit()) {
            manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        }
    }

    fun restoreAfterSession() {
        if (!prefs.getBoolean(KEY_APPLIED, false)) return
        if (hasAccess()) {
            val previous = prefs.getInt(KEY_PREVIOUS_FILTER, NotificationManager.INTERRUPTION_FILTER_ALL)
            runCatching { manager.setInterruptionFilter(previous) }
        }
        prefs.edit().remove(KEY_PREVIOUS_FILTER).putBoolean(KEY_APPLIED, false).apply()
    }

    companion object {
        private const val KEY_ENABLED = "enabled_for_focus"
        private const val KEY_PREVIOUS_FILTER = "previous_filter"
        private const val KEY_APPLIED = "applied"
    }
}
