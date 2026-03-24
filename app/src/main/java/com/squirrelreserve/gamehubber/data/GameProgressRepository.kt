package com.squirrelreserve.gamehubber.data

import android.content.Context
import androidx.room.withTransaction
import com.squirrelreserve.gamehubber.Difficulty
import com.squirrelreserve.gamehubber.GameStatus
import com.squirrelreserve.gamehubber.data.db.AppDatabase
import com.squirrelreserve.gamehubber.data.db.GameProgressEntity
import com.squirrelreserve.gamehubber.data.db.TokenTxnEntity
import com.squirrelreserve.gamehubber.data.db.TxnReason
import com.squirrelreserve.gamehubber.data.db.TxnType
import com.squirrelreserve.gamehubber.data.db.WalletEntity
import com.squirrelreserve.gamehubber.tokens.TokenCatalog
import com.squirrelreserve.gamehubber.tokens.TokenEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameProgressRepository (context: Context){
    private val db = AppDatabase.get(context)
    private val progressDao = AppDatabase.get(context).gameProgressDao()
    private val walletDao = db.walletDao()
    private val txnDao = db.tokenTxnDao()
    private val tokenRepo = TokenRepository(
        db = db,
        walletDao = walletDao,
        txnDao = txnDao
    )
    fun observeTodayProgress(): Flow<Map<String, GameProgressEntity>> {
        val today = DateKey.today()
        return progressDao.observeProgressForDate(today)
            .map{ list -> list.associateBy{ it.gameId}}
    }
    suspend fun markInProgress(gameId: String, difficulty: Difficulty){
        val today = DateKey.today()
        val now = System.currentTimeMillis()
        val current = progressDao.getProgress(gameId, today)
        progressDao.upsert(
            GameProgressEntity(
                gameId = gameId,
                dateKey = today,
                status = GameStatus.IN_PROGRESS.name,
                difficulty = difficulty.name,
                savedStateJson = current?.savedStateJson,
                rewardClaimed = current?.rewardClaimed ?: false,
                extraPlaysAvailable = current?.extraPlaysAvailable ?: 0,
                updatedAt = now
            )
        )
    }
    suspend fun markCompleted(gameId: String): Long{
        val today = DateKey.today()
        val now = System.currentTimeMillis()
        var earned = 0L
        db.withTransaction {
            val current = progressDao.getProgress(gameId, today)
            val difficulty = Difficulty.valueOf(current?.difficulty ?: Difficulty.EASY.name)
            val alreadyClaimed = current?.rewardClaimed ?: false
            if (!alreadyClaimed) {
                val rule = TokenCatalog.earnFor(TokenEvent.CompletedDaily(gameId, difficulty))
                if (rule != null && rule.amount > 0L) {
                    earned = rule.amount

                    val wallet = walletDao.getWallet() ?: WalletEntity(balance = 0L)
                    val newBalance = wallet.balance + earned
                    walletDao.upsertWallet(
                        wallet.copy(
                            balance = newBalance,
                            updatedAt = now
                        )
                    )
                    txnDao.insertTxn(
                        TokenTxnEntity(
                            type = TxnType.EARN,
                            reason = TxnReason.COMPLETE_DAILY,
                            amount = earned,
                            gameId = gameId,
                            dateKey = today,
                            updatedAt = now
                        )
                    )
                }
            }
            progressDao.upsert(
                GameProgressEntity(
                    gameId = gameId,
                    dateKey = today,
                    status = GameStatus.COMPLETED.name,
                    difficulty = difficulty.name,
                    rewardClaimed = alreadyClaimed || earned > 0L,
                    extraPlaysAvailable = current?.extraPlaysAvailable ?: 0,
                    savedStateJson = null,
                    updatedAt = now
                )
            )
        }
        return earned
    }
    suspend fun saveState(gameId: String, difficulty: Difficulty, json: String){
        val today = DateKey.today()
        val now = System.currentTimeMillis()
        val current = progressDao.getProgress(gameId, today)
        val newStatus = when (current?.status){
            GameStatus.COMPLETED.name -> GameStatus.COMPLETED.name
            else -> GameStatus.IN_PROGRESS.name
        }

        progressDao.upsert(
            GameProgressEntity(
                gameId = gameId,
                dateKey = today,
                status = newStatus,
                difficulty = difficulty.name,
                savedStateJson = json,
                rewardClaimed = current?.rewardClaimed ?: false,
                extraPlaysAvailable = current?.extraPlaysAvailable ?: 0,
                updatedAt = now
            )
        )
    }
    suspend fun clearState(gameId: String){
        val today = DateKey.today()
        val current = progressDao.getProgress(gameId, today) ?: return
        progressDao.upsert(current.copy(savedStateJson = null))
    }
    suspend fun getTodayProgress(gameId: String): GameProgressEntity?{
        return progressDao.getProgress(gameId, DateKey.today())
    }
    suspend fun loadState(gameId: String): String?{
        val today = DateKey.today()
        return progressDao.getProgress(gameId, today)?.savedStateJson
    }
    suspend fun resetToday(){
        progressDao.deleteForDate(DateKey.today())
    }
    suspend fun resetAllGameData(){
        progressDao.deleteAll()
    }
    suspend fun buyReplayForToday(
        progress: GameProgressEntity
    ): Boolean {
        val rule = TokenCatalog.costFor(TokenEvent.ReplayPurchased(progress.gameId)) ?: return false
        val costAbs = -rule.amount
        val ok = tokenRepo.spend(
            amount = costAbs,
            reason = TxnReason.REPLAY_GAME,
            gameId = progress.gameId,
            dateKey = progress.dateKey
        )
        if (!ok) return false
        val updated = progress.copy(
            extraPlaysAvailable = progress.extraPlaysAvailable + 1,
            updatedAt = System.currentTimeMillis()
        )
        progressDao.upsert(updated)
        return true
    }
    suspend fun startReplayIfNeeded(progress: GameProgressEntity): GameProgressEntity{
        if (progress.status == GameStatus.COMPLETED.name && progress.extraPlaysAvailable > 0){
            val updated = progress.copy(
                status = GameStatus.IN_PROGRESS.name,
                extraPlaysAvailable = progress.extraPlaysAvailable - 1,
                updatedAt = System.currentTimeMillis()
            )
            progressDao.upsert(updated)
            return updated
        }
        return progress
    }
}