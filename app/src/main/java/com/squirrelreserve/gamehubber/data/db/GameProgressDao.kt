package com.squirrelreserve.gamehubber.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameProgressDao {
    @Query("SELECT * FROM game_progress WHERE dateKey = :dateKey")
    fun observeProgressForDate(dateKey: String): Flow<List<GameProgressEntity>>
    @Query("SELECT * FROM game_progress WHERE gameId = :gameId AND dateKey = :dateKey LIMIT 1")
    suspend fun getProgress(gameId: String, dateKey: String): GameProgressEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: GameProgressEntity)
    @Query("DELETE FROM game_progress WHERE dateKey = :dateKey")
    suspend fun deleteForDate(dateKey: String)
    @Query("DELETE FROM game_progress")
    suspend fun deleteAll()
}