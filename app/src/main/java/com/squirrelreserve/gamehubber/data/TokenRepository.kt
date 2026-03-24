package com.squirrelreserve.gamehubber.data

import androidx.room.withTransaction
import com.squirrelreserve.gamehubber.data.db.AppDatabase
import com.squirrelreserve.gamehubber.data.db.TokenTxnDao
import com.squirrelreserve.gamehubber.data.db.TokenTxnEntity
import com.squirrelreserve.gamehubber.data.db.TxnReason
import com.squirrelreserve.gamehubber.data.db.TxnType
import com.squirrelreserve.gamehubber.data.db.WalletDao
import com.squirrelreserve.gamehubber.data.db.WalletEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TokenRepository (
    private val db: AppDatabase,
    private val walletDao: WalletDao,
    private val txnDao: TokenTxnDao
){
    suspend fun getBalance(): Long = walletDao.getWallet()?.balance ?: 0L
    suspend fun earn(
        amount: Long,
        reason: TxnReason,
        gameId: String? = null,
        dateKey: String? = null
    ){
        require(amount > 0)
        db.withTransaction {
            val wallet = walletDao.getWallet() ?: WalletEntity(balance = 0L)
            val newBalance = wallet.balance + amount
            walletDao.upsertWallet(wallet.copy(balance = newBalance, updatedAt = System.currentTimeMillis()))
            txnDao.insertTxn(
                TokenTxnEntity(
                    type = TxnType.EARN,
                    reason = reason,
                    amount = amount,
                    gameId = gameId,
                    dateKey = dateKey
                )
            )
        }
    }
    suspend fun spend(
        amount: Long,
        reason: TxnReason,
        gameId: String? = null,
        dateKey: String? = null
    ): Boolean{
        require(amount>0)
        return db.withTransaction {
            val wallet = walletDao.getWallet() ?: WalletEntity(balance = 0L)
            if (wallet.balance < amount) return@withTransaction false
            val newBalance = wallet.balance - amount
            walletDao.upsertWallet(wallet.copy(balance = newBalance, updatedAt = System.currentTimeMillis()))
            txnDao.insertTxn(
                TokenTxnEntity(
                    type = TxnType.SPEND,
                    reason = reason,
                    amount = -amount,
                    gameId = gameId,
                    dateKey = dateKey
                )
            )
            true
        }

    }
    fun observeBalance(userId: String = "local"): Flow<Long> =
        walletDao.observeBalance(userId).map { it ?: 0L }
}