package com.wenodh.zen.focus

data class FocusSessionRecord(
    val id: String,
    val startedAt: Long,
    val durationMinutes: Int,
    val status: String,
    val finishedAt: Long?,
    val blockedAttempts: Int
)
