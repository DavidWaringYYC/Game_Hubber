package com.squirrelreserve.gamehubber.games.memory

import com.squirrelreserve.gamehubber.Difficulty

object BoardConfig{
    fun rowsFor(difficulty: Difficulty): Int = when (difficulty){
        Difficulty.EASY -> 3
        Difficulty.MEDIUM -> 4
        Difficulty.HARD -> 5
    }
    const val COLS = 4
}
data class SymbolSpec(
    val iconRes: Int,
    val matchedColor: Int
)