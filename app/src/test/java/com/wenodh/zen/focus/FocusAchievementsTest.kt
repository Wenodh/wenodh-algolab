package com.wenodh.zen.focus

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusAchievementsTest {
    @Test
    fun achievementsUnlockAtTheirThresholds() {
        val result = FocusAchievements.evaluate(5, 300, 1, 5)
        assertTrue(result.all { it.unlocked })
    }

    @Test
    fun achievementsRemainLockedBelowThresholds() {
        val result = FocusAchievements.evaluate(0, 299, 0, 4)
        assertFalse(result.any { it.unlocked })
    }
}
