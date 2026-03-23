package com.squirrelreserve.gamehubber

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.squirrelreserve.gamehubber.data.TokenRepository
import com.squirrelreserve.gamehubber.data.db.AppDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class TokenFlowSmokeTest {
    private lateinit var db: AppDatabase
    private lateinit var repo: TokenRepository
    @Before
    fun setup(){
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = TokenRepository(
            db = db,
            walletDao = db.walletDao(),
            txnDao = db.tokenTxnDao()
        )
    }
    @After
    fun tearDown(){
        db.close()
    }
    @Test
    fun token_balance_flow_emits_on_earn_and_spend() = runTest{
        val balanceFlow = db.walletDao().observeBalance().map { it ?: 0L}
        assertEquals(0L, balanceFlow.first())
        assertEquals(0L, repo.getBalance())
        repo.earn(amount = 10, reason = "COMPLETE_DAILY", gameId = GameIds.MEMORY_MATCH, dateKey = "2026-03-17")
        assertEquals(10L, repo.getBalance())
        assertEquals(10L, balanceFlow.first())
        val ok = repo.spend(amount = 4, reason = "HINT_WORDSEARCH", gameId = GameIds.WORD_SEARCH, dateKey = "2026-03-17")
        assertTrue(ok)
        assertEquals(6L, repo.getBalance())
        assertEquals(6L, balanceFlow.first())
        val ok2 = repo.spend(amount = 999, reason = "REPLAY", gameId = GameIds.WORD_SEARCH, dateKey = "2026-03-17")
        assertTrue(ok2)
        assertEquals(6L, repo.getBalance())
        assertEquals(6L, balanceFlow.first())
    }
    @Test
    fun txns_written_with_correct_signs() = runTest{
        repo.earn(10,"COMPLETE_DAILY", GameIds.MEMORY_MATCH,"2026-03-17")
        repo.spend(7,"HINT_WORDSEARCH", GameIds.WORD_SEARCH,"2026-03-17")
        val txns = db.tokenTxnDao().observeTxns().first()
        val amounts = txns.map { it.amount }
        assertTrue(amounts.contains(10L))
        assertTrue(amounts.contains(-7L))
    }


}