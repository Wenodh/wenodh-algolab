package com.wenodh.zen.focus

data class FocusAchievement(
    val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean
)

object FocusAchievements {
    fun evaluate(
        completedSessions: Int,
        completedMinutes: Int,
        challengeExits: Int,
        blockedAttempts: Int
    ): List<FocusAchievement> = listOf(
        FocusAchievement("first_session", "First focus", "Complete your first focus session.", completedSessions >= 1),
        FocusAchievement("five_sessions", "Building a rhythm", "Complete five focus sessions.", completedSessions >= 5),
        FocusAchievement("five_hours", "Five-hour milestone", "Accumulate five hours of completed focus.", completedMinutes >= 300),
        FocusAchievement("intentional_exit", "Intentional choices", "Use an exit challenge at least once.", challengeExits >= 1),
        FocusAchievement("focus_guardian", "Focus guardian", "Reach five blocked-app interruptions.", blockedAttempts >= 5)
    )
}
