package com.wenodh.zen.focus

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class FocusChallengeTest {
    @Test
    fun generatedChallengeHasExpectedAnswer() {
        repeat(100) {
            val challenge = FocusChallenge.create(level = 2, random = Random(it))
            val answer = challenge.expectedAnswer.toString()
            assertTrue(FocusChallenge.isCorrect(answer, challenge))
        }
    }

    @Test
    fun incorrectAndMalformedAnswersAreRejected() {
        val challenge = FocusChallenge.create(random = Random(12))
        assertFalse(FocusChallenge.isCorrect("not a number", challenge))
        assertFalse(FocusChallenge.isCorrect((challenge.expectedAnswer + 1).toString(), challenge))
    }

    @Test
    fun trimsUserInput() {
        val challenge = FocusChallenge.create(random = Random(5))
        assertTrue(FocusChallenge.isCorrect("  ${challenge.expectedAnswer}  ", challenge))
    }
}
