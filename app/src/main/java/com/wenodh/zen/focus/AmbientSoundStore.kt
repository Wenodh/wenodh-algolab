package com.wenodh.zen.focus

import android.content.Context

class AmbientSoundStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("ambient_sound", Context.MODE_PRIVATE)
    fun selected(): AmbientSoundEngine.Sound? =
        prefs.getString(KEY_SOUND, null)?.let { runCatching { AmbientSoundEngine.Sound.valueOf(it) }.getOrNull() }
    fun select(sound: AmbientSoundEngine.Sound?) {
        val editor = prefs.edit()
        if (sound == null) editor.remove(KEY_SOUND) else editor.putString(KEY_SOUND, sound.name)
        check(editor.commit())
    }
    companion object { private const val KEY_SOUND = "selected_sound" }
}
