package com.squirrelreserve.gamehubber.games.memory

import kotlinx.serialization.Serializable

@Serializable
data class CardState(
    val pairId: Int,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

@Serializable
data class MemoryMatchState(
    val schemaVersion: Int = 1,
    val difficulty: String,
    val rows: Int,
    val cols: Int = 4,
    val seed: Long,
    val moves: Int = 0,
    val firstSelectedIndex: Int? = null,
    val cards: List<CardState>
)