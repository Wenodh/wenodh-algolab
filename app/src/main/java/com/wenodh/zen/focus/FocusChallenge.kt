package com.wenodh.zen.focus

import kotlin.random.Random

data class MathChallenge(
    val prompt: String,
    val expectedAnswer: Int
)

object FocusChallenge {
    fun create(level: Int = 1, random: Random = Random.Default): MathChallenge {
        val safeLevel = level.coerceIn(1, 3)
        val max = when (safeLevel) {
            1 -> 9
            2 -> 20
            else -> 50
        }
        val a = random.nextInt(2, max + 1)
        val b = random.nextInt(2, max + 1)
        return if (random.nextBoolean()) {
            MathChallenge("$a + $b = ?", a + b)
        } else {
            val larger = maxOf(a, b)
            val smaller = minOf(a, b)
            MathChallenge("$larger − $smaller = ?", larger - smaller)
        }
    }

    fun isCorrect(input: String, challenge: MathChallenge): Boolean =
        input.trim().toIntOrNull() == challenge.expectedAnswer
}
