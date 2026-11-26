package com.wenodh.zen.focus

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class FocusChallengeTest {
    @Test
    fun generatedMathChallengeHasExpectedAnswer() {
        repeat(100) {
            val challenge = FocusChallenge.create(level = 2, random = Random(it))
            assertTrue(FocusChallenge.isCorrect(challenge.expectedAnswer.toString(), challenge))
        }
    }

    @Test
    fun incorrectAndMalformedMathAnswersAreRejected() {
        val challenge = FocusChallenge.create(random = Random(12))
        assertFalse(FocusChallenge.isCorrect("not a number", challenge))
        assertFalse(FocusChallenge.isCorrect((challenge.expectedAnswer + 1).toString(), challenge))
    }

    @Test
    fun memoryChallengeRequiresExactSequence() {
        val challenge = MemoryChallenge("12049")
        assertTrue(FocusChallenge.isCorrect("12 049", challenge))
        assertFalse(FocusChallenge.isCorrect("12048", challenge))
    }

    @Test
    fun reflectionRequiresMeaningfulResponse() {
        val challenge = FocusChallenge.createReflection()
        assertFalse(FocusChallenge.isCorrect("done", challenge))
        assertTrue(FocusChallenge.isCorrect("I wanted to finish a focused reading session.", challenge))
    }

    @Test
    fun trimsMathInput() {
        val challenge = FocusChallenge.create(random = Random(5))
        assertTrue(FocusChallenge.isCorrect("  ${challenge.expectedAnswer}  ", challenge))
    }
}
