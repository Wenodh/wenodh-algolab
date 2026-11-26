package com.wenodh.zen.focus

import android.content.Context

class FocusProfileStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("focus_profiles", Context.MODE_PRIVATE)

    fun selectedId(): String = prefs.getString(KEY_SELECTED, "study") ?: "study"

    fun select(id: String) {
        require(DefaultFocusProfiles.all.any { it.id == id } || id == CUSTOM_ID)
        check(prefs.edit().putString(KEY_SELECTED, id).commit())
    }

    fun customProfile(): FocusProfile? {
        val name = prefs.getString(KEY_CUSTOM_NAME, null) ?: return null
        return FocusProfile(CUSTOM_ID, name, prefs.getInt(KEY_CUSTOM_DURATION, 25).coerceIn(5, 180), 2)
    }

    fun saveCustom(name: String, durationMinutes: Int) {
        require(name.isNotBlank())
        require(durationMinutes in 5..180)
        check(
            prefs.edit()
                .putString(KEY_CUSTOM_NAME, name.trim().take(32))
                .putInt(KEY_CUSTOM_DURATION, durationMinutes)
                .putString(KEY_SELECTED, CUSTOM_ID)
                .commit()
        )
    }

    fun profiles(): List<FocusProfile> = DefaultFocusProfiles.all + listOfNotNull(customProfile())

    fun selectedProfile(): FocusProfile =
        profiles().firstOrNull { it.id == selectedId() } ?: DefaultFocusProfiles.all[1]

    companion object {
        const val CUSTOM_ID = "custom"
        private const val KEY_SELECTED = "selected_profile"
        private const val KEY_CUSTOM_NAME = "custom_profile_name"
        private const val KEY_CUSTOM_DURATION = "custom_profile_duration"
    }
}
