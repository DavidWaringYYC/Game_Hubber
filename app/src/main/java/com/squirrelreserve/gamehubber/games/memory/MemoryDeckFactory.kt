package com.squirrelreserve.gamehubber.games.memory

import com.squirrelreserve.gamehubber.Difficulty
import kotlin.random.Random

object MemoryDeckFactory {
    fun newGameState(difficulty: String, rows: Int, cols: Int, seed: Long): MemoryMatchState{
        val totalCards = rows * cols
        require(totalCards %2 == 0){"Total cards must be even."}
        val pairs = totalCards / 2
        val ids = (0 until pairs).flatMap { listOf(it, it) }
        val rng = Random(seed)
        val shuffled = ids.shuffled(rng).map{ pairId ->
            CardState(pairId = pairId)
        }
        return MemoryMatchState(
            difficulty = difficulty,
            rows = rows,
            cols = cols,
            seed = seed,
            cards = shuffled
        )

    }
}