package com.squirrelreserve.gamehubber.tokens

import com.squirrelreserve.gamehubber.Difficulty
import com.squirrelreserve.gamehubber.GameIds
import com.squirrelreserve.gamehubber.data.db.TxnReason

sealed interface TokenEvent {
    data class CompletedDaily(val gameId: String, val difficulty: Difficulty): TokenEvent
    data class ReplayPurchased(val gameId: String): TokenEvent
    data class HintPurchased(val gameId: String, val hintType: HintType): TokenEvent
}
enum class  HintType{
    WORDSEARCH_REVEAL_FIRST_LETTER,
    WORDSEARCH_REVEAL_WORD,
    MEMORYMATCH_PEEK
}
data class TokenRuleResult(
    val amount: Long,
    val reason: TxnReason,
    val description: String = ""
)
object TokenCatalog {
    fun earnFor(event: TokenEvent): TokenRuleResult?{
        return when (event){
            is TokenEvent.CompletedDaily -> {
                val base = when (event.gameId){
                    GameIds.MEMORY_MATCH -> 10L
                    GameIds.WORD_SEARCH -> 10L
                    else -> 0L
                }
                if (base <= 0L) null
                else {
                    val multiplier = when (event.difficulty){
                        Difficulty.EASY -> 1.0
                        Difficulty.MEDIUM -> 1.5
                        Difficulty.HARD -> 2.0
                    }
                    val earned = (base * multiplier).toLong()
                    TokenRuleResult(
                        amount = earned,
                        reason = TxnReason.COMPLETE_DAILY,
                        description = "Daily completion reward"
                    )
                }
            }
            else -> null
        }
    }
    fun costFor(event: TokenEvent): TokenRuleResult? {
        return when (event){
            is TokenEvent.ReplayPurchased -> TokenRuleResult(
                amount = -25L,
                reason = TxnReason.REPLAY_GAME,
                description = "Play again today"
            )
            is TokenEvent.HintPurchased ->{
                val cost = when (event.gameId){
                    GameIds.WORD_SEARCH -> when (event.hintType){
                        HintType.WORDSEARCH_REVEAL_FIRST_LETTER -> 10L
                        HintType.WORDSEARCH_REVEAL_WORD -> 25L
                        else -> 10L
                    }

                    GameIds.MEMORY_MATCH -> when(event.hintType){
                        HintType.MEMORYMATCH_PEEK -> 8L
                        else ->8L
                    }
                    else -> 0L
                }
                val reason = when (event.gameId){
                    GameIds.MEMORY_MATCH -> TxnReason.HINT_MEMORY_MATCH
                    GameIds.WORD_SEARCH -> TxnReason.HINT_WORDSEARCH
                    else -> null
                }
                if (cost <= 0L) null else TokenRuleResult(
                    amount = -cost,
                    reason = reason!!,
                    description = "Hint Purchase"

                )
            }
            else -> null
        }
    }
}