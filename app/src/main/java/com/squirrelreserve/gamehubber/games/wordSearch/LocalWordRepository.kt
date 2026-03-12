package com.squirrelreserve.gamehubber.games.wordSearch

import com.squirrelreserve.gamehubber.Difficulty

class LocalWordRepository : WordRepository {
    private val pool = listOf(
        "SQUIRREL","LEAF","MOON","STAR","CLOUD","MUSIC","DIAMOND","FLOWER",
        "LIGHTNING","HEART","JIGSAW","SUN","RIVER","MOUNTAIN","FOREST","PUZZLE"
    )
    override suspend fun getWords(difficulty: Difficulty): List<String>{
        val count = when (difficulty){
            Difficulty.EASY -> 6
            Difficulty.MEDIUM -> 9
            Difficulty.HARD -> 12
        }
        return pool.shuffled().take(count).map{ it.uppercase()}
    }
}