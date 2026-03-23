package com.squirrelreserve.gamehubber.games.wordSearch

import kotlin.random.Random

object WordSearchGenerator {
    private val directions = listOf(
        0 to 1, //right
        0 to -1, //left
        1 to 0, //down
        -1 to 0, //up
        1 to 1, //down-right
        1 to -1, //down-left
        -1 to 1, //up-right
        -1 to -1 //up-left
    )

    fun generate(rows: Int, cols: Int, words: List<String>, seed: Long): String {
        val upperWords = words.map { it.uppercase() }
            .sortedByDescending { it.length }
        var attemptSeed = seed
        repeat(50) {
            val rng = Random(attemptSeed)
            val grid = CharArray(rows * cols) { '\u0000' }
            var allPlaced = true
            for (word in upperWords) {
                if (!placeWord(grid, rows, cols, word, rng)) {
                    allPlaced = false
                    break
                }
            }
            if (allPlaced) {
                for (i in grid.indices) {
                    if (grid[i] == '\u0000') {
                        grid[i] = ('A'..'Z').random(rng)
                    }
                }
                return grid.concatToString()
            }
            attemptSeed = rng.nextLong()
        }
        throw IllegalStateException("Unable to generate word search grid after many attempts.")
    }

    private fun overlapScore(
        grid: CharArray, rows: Int, cols: Int,
        word: String, r0: Int, c0: Int, dr: Int, dc: Int
    ): Int {
        var r = r0
        var c = c0
        var score = 0
        for (ch in word) {
            val existing = grid[r * cols + c]
            if (existing == ch) score++
            r += dr
            c += dc
        }
        return score
    }

    private fun placeWord(
        grid: CharArray,
        rows: Int,
        cols: Int,
        word: String,
        rng: Random
    ): Boolean {
        var best: Placement? = null
        repeat(500) {
            val (dr, dc) = directions.random(rng)
            val r0 = rng.nextInt(rows)
            val c0 = rng.nextInt(cols)
            if (!canPlace(grid, rows, cols, word, r0, c0, dr, dc)) return@repeat
            val score = overlapScore(grid, rows, cols, word, r0, c0, dr, dc)
            if (best == null || score > best!!.score) {
                best = Placement(r0, c0, dr, dc, score)
                if (score == word.length) return@repeat
            }
        }
        val chosen = best ?: return false
        var r = chosen.r0
        var c = chosen.c0
        for (ch in word) {
            grid[r * cols + c] = ch
            r += chosen.dr
            c += chosen.dc
        }
        return true
    }

    private fun canPlace(
        grid: CharArray,
        rows: Int,
        cols: Int,
        word: String,
        r0: Int,
        c0: Int,
        dr: Int,
        dc: Int
    ): Boolean {
        var r = r0
        var c = c0
        for (ch in word) {
            if (r !in 0 until rows || c !in 0 until cols) return false
            val existing = grid[r * cols + c]
            if (existing != '\u0000' && existing != ch) return false
            r += dr
            c += dc
        }
        return true
    }
}
private data class Placement(val r0: Int, val c0: Int, val dr: Int, val dc: Int, val score: Int)