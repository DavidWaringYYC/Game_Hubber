package com.squirrelreserve.gamehubber.games.wordSearch

import com.squirrelreserve.gamehubber.Difficulty

interface WordRepository {
    suspend fun getWords(difficulty: Difficulty): List<String>
}