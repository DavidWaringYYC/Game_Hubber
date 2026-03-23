package com.squirrelreserve.gamehubber.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenTxnDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTxn(txn: TokenTxnEntity)
    @Query("SELECT * FROM token_txn WHERE userId = :userId ORDER BY updatedAt DESC")
    fun observeTxns(userId: String = "local"): Flow<List<TokenTxnEntity>>
    @Query("""
        SELECT EXISTS(
            SELECT 1 from token_txn
            WHERE userId = :userId
            AND reason = :reason
            AND gameId = :gameId
            AND dateKey = :dateKey
            LIMIT 1
        )
    """)
    suspend fun hasTxnFor(reason: String, gameId: String, dateKey: String, userId: String = "local"): Boolean
    @Query("SELECT COALESCE(SUM(amount),0) FROM token_txn WHERE userId = :userId")
    suspend fun getNetTotal(userId: String = "local"): Long
}