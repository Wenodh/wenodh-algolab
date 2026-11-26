package com.wenodh.zen.focus

data class FocusProfile(
    val id: String,
    val label: String,
    val durationMinutes: Int,
    val challengeLevel: Int
)

object DefaultFocusProfiles {
    val all = listOf(
        FocusProfile("work", "Work", 45, 2),
        FocusProfile("study", "Study", 25, 1),
        FocusProfile("reading", "Reading", 30, 1),
        FocusProfile("sleep", "Wind down", 60, 1)
    )
}
