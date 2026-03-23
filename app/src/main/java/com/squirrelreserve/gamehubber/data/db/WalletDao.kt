package com.squirrelreserve.gamehubber.data.db

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Insert
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet WHERE userId = :userId LIMIT 1")
    suspend fun getWallet (userId: String = "local"): WalletEntity?
    @Query("SELECT COALESCE((SELECT balance FROM wallet WHERE userId = :userId LIMIT 1),0)")
    fun observeBalance(userId: String = "local"): Flow<Long?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWallet(wallet: WalletEntity)
}