package com.squirrelreserve.gamehubber.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
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
    val updatedAt: Long
)
@Entity(
    tableName = "token_txn"
)
data class TokenTxnEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "local",
    val updatedAt: Long = System.currentTimeMillis(),
    val type: TxnType,
    val reason: TxnReason,
    val amount: Long,
    val gameId: String? = null,
    val dateKey: String? = null
)
@Entity(tableName = "wallet")
data class WalletEntity(
    @PrimaryKey
    val userId: String = "local",
    val balance: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)
enum class TxnType {
    EARN,
    SPEND
}
enum class TxnReason{
    COMPLETE_DAILY,
    REPLAY_GAME,
    HINT_WORDSEARCH,
    HINT_MEMORY_MATCH
}