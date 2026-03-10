package com.squirrelreserve.gamehubber

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squirrelreserve.gamehubber.data.GameProgressRepository
import com.squirrelreserve.gamehubber.games.memory.BoardConfig
import com.squirrelreserve.gamehubber.games.memory.MemoryCardAdapter
import com.squirrelreserve.gamehubber.games.memory.MemoryDeckFactory
import com.squirrelreserve.gamehubber.games.memory.MemoryMatchState
import com.squirrelreserve.gamehubber.serialization.JsonProvider
import com.squirrelreserve.gamehubber.ui.setupToolbar
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.floor

class MemoryMatchFragment : Fragment(R.layout.fragment_memory_match) {
    private val args: MemoryMatchFragmentArgs by navArgs()
    private val repo by lazy{
        GameProgressRepository(requireContext().applicationContext)
    }
    private var state: MemoryMatchState? = null
    private lateinit var adapter: MemoryCardAdapter
    private var lockInput: Boolean = false
    private var completedHandled = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val difficulty = Difficulty.valueOf(args.difficulty)
        val rows = BoardConfig.rowsFor(difficulty)
        val cols = BoardConfig.COLS

        val toolbar = view.setupToolbar(findNavController())
        toolbar.inflateMenu(R.menu.menu_game_hub)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    findNavController().navigate(R.id.action_global_to_settingsFragment)
                    true
                }
                else -> false
            }
        }

        val rv = view.findViewById<RecyclerView>(R.id.rvGrid)
        rv.layoutManager = GridLayoutManager(requireContext(), cols)
        adapter = MemoryCardAdapter{ index -> onCardTapped(index)}
        rv.adapter = adapter

        rv.post {
            val totalWidth = rv.width
            val cellSize = floor(totalWidth / cols.toFloat()).toInt()
            adapter.setItemSize(cellSize)
        }
        state = MemoryDeckFactory.newGameState(
            difficulty = difficulty.name,
            rows = rows,
            cols = cols,
            seed = System.currentTimeMillis()
        )
        render(view)


        viewLifecycleOwner.lifecycleScope.launch {
            val json = repo.loadState("memory_match")
            if (!json.isNullOrBlank()) {
                state = JsonProvider.json.decodeFromString<MemoryMatchState>(json)
                render(view)
            }
        }
       view.findViewById<MaterialButton>(R.id.btnComplete).setOnClickListener {
           lifecycleScope.launch {
               repo.markCompleted("memory_match")
               val navController = findNavController()
               navController.navigateUp()
           }
       }
    }

    override fun onStop() {
        super.onStop()
        val currentState = state ?: return
        if (completedHandled) return
        val difficulty = Difficulty.valueOf(args.difficulty)
        lifecycleScope.launch {
            withContext(NonCancellable) {
                val json = JsonProvider.json.encodeToString(currentState)
                repo.saveState("memory_match", difficulty, json)
            }
        }
    }
    private fun onCardTapped(index: Int){
        if(lockInput) return
        val s = state?: return
        val card = s.cards[index]
        if (card.isMatched || card.isFaceUp) return
        val cards = s.cards.toMutableList()
        cards[index] = card.copy(isFaceUp = true)
        val first = s.firstSelectedIndex
        if (first == null){
            state = s.copy(cards = cards, firstSelectedIndex = index)
            render(requireView())
            return
        }
        val firstCard = cards[first]
        val secondCard = cards[index]
        val moves = s.moves + 1
        if(firstCard.pairId == secondCard.pairId){
            cards[first] = firstCard.copy(isMatched = true)
            cards[index] = secondCard.copy(isMatched = true)
            val newState = s.copy(
                cards = cards,
                firstSelectedIndex = null,
                moves = moves
            )
            state = newState
            render(requireView())

            if(newState.cards.all { it.isMatched }){
               onGameCompleted()
            }
        } else {
            state = s.copy(cards = cards, firstSelectedIndex = null, moves = moves)
            render(requireView())
            lockInput = true
            Handler(Looper.getMainLooper()).postDelayed({
                val current = state?: return@postDelayed
                val newCards = current.cards.toMutableList()
                newCards[first] = newCards[first].copy(isFaceUp = false)
                newCards[index] = newCards[index].copy(isFaceUp = false)
                state = current.copy(cards = newCards)
                render(requireView())
                lockInput = false
            }, 650)
        }
    }
    private fun render(view: View){
        val s = state ?: return
        val matched = matchedPairsCount(s)
        val total = totalPairs(s)
        view.findViewById<TextView>(R.id.tvDifficulty).text = "Difficulty: ${s.difficulty} (${s.rows}x${s.cols})"
        view.findViewById<TextView>(R.id.tvMoves).text = "Moves: ${s.moves}"
        view.findViewById<TextView>(R.id.tvMatches).text = "Matches: ${matched} / ${total} "
        adapter.submit(s.cards)
    }
    private fun onGameCompleted(){
        if (completedHandled) return
        completedHandled = true
        lockInput = true
        lifecycleScope.launch {
            repo.markCompleted("memory_match")
            repo.clearState("memory_match")
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Game Completed")
            .setMessage("Great Job! what would you like to do next?")
            .setCancelable(false)
            .setPositiveButton("Back to Hub"){ _,_ -> findNavController().navigateUp()}
            .setNegativeButton("Play Again") {_,_ -> startNewGameSameDifficulty()}
            .show()
    }
    private fun startNewGameSameDifficulty(){
        val view = requireView()
        val difficulty = Difficulty.valueOf(args.difficulty)
        val rows = BoardConfig.rowsFor(difficulty)
        val cols = BoardConfig.COLS
        state = MemoryDeckFactory.newGameState(
            difficulty = difficulty.name,
            rows = rows,
            cols = cols,
            seed = System.currentTimeMillis(),
        )
        completedHandled = false
        lockInput = false
        render(view)
    }
    private fun matchedPairsCount(state: MemoryMatchState): Int {
        return state.cards.count{ it.isMatched } / 2
    }
    private fun totalPairs(state: MemoryMatchState) : Int{
        return(state.rows * state.cols) / 2
    }
}