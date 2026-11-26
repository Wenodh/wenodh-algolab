package com.wenodh.zen.focus

/** Pure time/state rules so expiry behavior can be tested without Android. */
object SessionTimeline {
    fun remainingMillis(endsAtMillis: Long, nowMillis: Long): Long =
        (endsAtMillis - nowMillis).coerceAtLeast(0L)

    fun isExpired(endsAtMillis: Long, nowMillis: Long): Boolean =
        endsAtMillis <= nowMillis

    fun terminalStatus(interruptedByUser: Boolean): String =
        if (interruptedByUser) STATUS_INTERRUPTED else STATUS_COMPLETED

    const val STATUS_ACTIVE = "ACTIVE"
    const val STATUS_COMPLETED = "COMPLETED"
    const val STATUS_INTERRUPTED = "INTERRUPTED"
    const val STATUS_CHALLENGE_EXIT = "CHALLENGE_EXIT"
}
