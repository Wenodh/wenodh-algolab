package com.wenodh.zen.focus

import kotlin.random.Random

sealed interface ExitChallenge {
    val prompt: String
    fun accepts(input: String): Boolean
}

data class MathChallenge(
    override val prompt: String,
    val expectedAnswer: Int
) : ExitChallenge {
    override fun accepts(input: String): Boolean = input.trim().toIntOrNull() == expectedAnswer
}

data class MemoryChallenge(val sequence: String) : ExitChallenge {
    override val prompt: String = "Memorize the sequence, then enter it from memory."
    override fun accepts(input: String): Boolean = input.filter(Char::isDigit) == sequence
}

data class ReflectionChallenge(
    override val prompt: String = "What were you trying to accomplish before this session ends?"
) : ExitChallenge {
    override fun accepts(input: String): Boolean = input.trim().length >= 20
}

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

    fun createMemory(random: Random = Random.Default): MemoryChallenge =
        MemoryChallenge((1..5).joinToString("") { random.nextInt(0, 10).toString() })

    fun createReflection(): ReflectionChallenge = ReflectionChallenge()

    fun isCorrect(input: String, challenge: ExitChallenge): Boolean = challenge.accepts(input)
}
