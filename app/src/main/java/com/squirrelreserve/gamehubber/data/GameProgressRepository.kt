package com.squirrelreserve.gamehubber.data

import android.content.Context
import com.squirrelreserve.gamehubber.Difficulty
import com.squirrelreserve.gamehubber.GameStatus
import com.squirrelreserve.gamehubber.data.db.AppDatabase
import com.squirrelreserve.gamehubber.data.db.GameProgressEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameProgressRepository (context: Context){
    private val dao = AppDatabase.get(context).gameProgressDao()
    fun observeTodayProgress(): Flow<Map<String, GameProgressEntity>> {
        val today = DateKey.today()
        return dao.observerProgressForData(today)
            .map{ list -> list.associateBy{ it.gameId}}
    }
    suspend fun markInProgress(gameId: String, difficulty: Difficulty){
        val today = DateKey.today()
        val now = System.currentTimeMillis()
        val current = dao.getProgress(gameId, today)
        dao.upsert(
            GameProgressEntity(
                gameId = gameId,
                dateKey = today,
                status = GameStatus.IN_PROGRESS.name,
                difficulty = difficulty.name,
                savedStateJson = current?.savedStateJson,
                updatedAt = now
            )
        )
    }
    suspend fun markCompleted(gameId: String){
        val today = DateKey.today()
        val now = System.currentTimeMillis()
        val current = dao.getProgress(gameId, today)
        val difficulty = current?.difficulty ?: Difficulty.EASY.name

        dao.upsert(
            GameProgressEntity(
                gameId = gameId,
                dateKey = today,
                status = GameStatus.COMPLETED.name,
                difficulty = difficulty,
                savedStateJson = null,
                updatedAt = now
            )
        )
    }
    suspend fun saveState(gameId: String, difficulty: Difficulty, json: String){
        val today = DateKey.today()
        val now = System.currentTimeMillis()
        val current = dao.getProgress(gameId, today)
        val newStatus = when (current?.status){
            GameStatus.COMPLETED.name -> GameStatus.COMPLETED.name
            else -> GameStatus.IN_PROGRESS.name
        }

        dao.upsert(
            GameProgressEntity(
                gameId = gameId,
                dateKey = today,
                status = newStatus,
                difficulty = difficulty.name,
                savedStateJson = json,
                updatedAt = now
            )
        )
    }
    suspend fun clearState(gameId: String){
        val today = DateKey.today()
        val current = dao.getProgress(gameId, today) ?: return
        dao.upsert(current.copy(savedStateJson = null))
    }
    suspend fun getTodayProgress(gameId: String): GameProgressEntity?{
        return dao.getProgress(gameId, DateKey.today())
    }
    suspend fun loadState(gameId: String): String?{
        val today = DateKey.today()
        return dao.getProgress(gameId, today)?.savedStateJson
    }
}