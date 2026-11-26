package com.wenodh.zen.focus

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionTimelineTest {
    @Test
    fun remainingTimeNeverBecomesNegative() {
        assertEquals(5_000L, SessionTimeline.remainingMillis(15_000L, 10_000L))
        assertEquals(0L, SessionTimeline.remainingMillis(9_000L, 10_000L))
    }

    @Test
    fun deadlineDeterminesExpiryAcrossProcessRestarts() {
        val persistedDeadline = 60_000L
        assertFalse(SessionTimeline.isExpired(persistedDeadline, 59_999L))
        assertTrue(SessionTimeline.isExpired(persistedDeadline, 60_000L))
        assertTrue(SessionTimeline.isExpired(persistedDeadline, 90_000L))
    }

    @Test
    fun challengeExitIsNotRecordedAsCompleted() {
        assertEquals("CHALLENGE_EXIT", SessionTimeline.STATUS_CHALLENGE_EXIT)
        assertTrue(SessionTimeline.STATUS_CHALLENGE_EXIT != SessionTimeline.STATUS_COMPLETED)
    }

    @Test
    fun completionAndManualExitHaveDifferentTerminalStates() {
        assertEquals(SessionTimeline.STATUS_COMPLETED, SessionTimeline.terminalStatus(false))
        assertEquals(SessionTimeline.STATUS_INTERRUPTED, SessionTimeline.terminalStatus(true))
    }
}
