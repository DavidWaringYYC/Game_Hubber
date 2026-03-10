package com.squirrelreserve.gamehubber.games.wordSearch

data class WordSearchState (
    val schemaVersion: Int = 1,
    val difficulty: String,
    val wordsFound: Int = 0,
    val elapsedMs: Long = 0L
)