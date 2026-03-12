package com.squirrelreserve.gamehubber.games.wordSearch

import kotlinx.serialization.Serializable

@Serializable
data class WordSearchState (
    val schemaVersion: Int = 1,
    val difficulty: String,
    val rows: Int = 8,
    val cols: Int = 8,
    val grid: String,
    val words: List<String>,
    val found: Set<String> = emptySet(),
    val cellColors: Map<Int, Int> = emptyMap(),
    val wordColors: Map<String, Int> = emptyMap(),
    val elapsedMs: Long = 0L,
    val foundLines: List<FoundLine> = emptyList()
)
@Serializable
data class FoundLine(
    val startIndex: Int,
    val endIndex: Int,
    val color: Int
)