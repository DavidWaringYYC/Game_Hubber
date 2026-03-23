package com.squirrelreserve.gamehubber.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squirrelreserve.gamehubber.Difficulty
import java.util.UUID

@Entity(
    tableName = "game_progress",
    primaryKeys = ["gameId","dateKey"]
)
data class GameProgressEntity(
    val gameId: String,
    val dateKey: String,
    val status: String,
    val difficulty: String,
    val rewardClaimed: Boolean = false,
    val extraPlaysAvailable: Int = 0,
    val savedStateJson: String?,
    val updatedAt: Long = System.currentTimeMillis()
)
@Entity(
    tableName = "wallet"
)
data class  WalletEntity(
    @PrimaryKey val userId: String = "local",
    val balance: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)
@Entity(
    tableName = "token_txn"
)
data class TokenTxnEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "local",
    val type: String,
    val reason: String,
    val amount: Long,
    val gameId: String? = null,
    val dateKey: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)