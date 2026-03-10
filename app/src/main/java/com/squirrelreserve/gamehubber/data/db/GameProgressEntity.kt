package com.squirrelreserve.gamehubber.data.db

import androidx.room.Entity
import com.squirrelreserve.gamehubber.Difficulty

@Entity(
    tableName = "game_progress",
    primaryKeys = ["gameId","dateKey"]
)
data class GameProgressEntity(
    val gameId: String,
    val dateKey: String,
    val status: String,
    val difficulty: String,
    val savedStateJson: String?,
    val updatedAt: Long
)