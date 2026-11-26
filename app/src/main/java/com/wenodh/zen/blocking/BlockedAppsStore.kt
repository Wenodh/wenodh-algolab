package com.wenodh.zen.blocking

import android.content.Context

class BlockedAppsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("app_blocking", Context.MODE_PRIVATE)

    fun blockedPackages(): Set<String> = prefs.getStringSet(KEY_BLOCKED, emptySet()).orEmpty().toSet()

    fun setBlocked(packageName: String, blocked: Boolean) {
        val updated = blockedPackages().toMutableSet()
        if (blocked) updated.add(packageName) else updated.remove(packageName)
        check(prefs.edit().putStringSet(KEY_BLOCKED, updated).commit()) {
            "Could not save blocked app selection."
        }
    }

    fun hasAcceptedDisclosure(): Boolean = prefs.getBoolean(KEY_DISCLOSURE_ACCEPTED, false)

    fun acceptDisclosure() {
        check(prefs.edit().putBoolean(KEY_DISCLOSURE_ACCEPTED, true).commit()) {
            "Could not save Accessibility disclosure consent."
        }
    }

    companion object {
        private const val KEY_BLOCKED = "blocked_packages"
        private const val KEY_DISCLOSURE_ACCEPTED = "accessibility_disclosure_accepted"
    }
}
