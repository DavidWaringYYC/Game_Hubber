package com.squirrelreserve.gamehubber

enum class GameStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}
data class GameInfo(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val status: GameStatus
)