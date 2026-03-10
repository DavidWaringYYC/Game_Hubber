package com.squirrelreserve.gamehubber

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.squirrelreserve.gamehubber.data.GameProgressRepository
import com.squirrelreserve.gamehubber.ui.setupToolbar
import kotlinx.coroutines.launch

class GameHubFragment : Fragment(R.layout.fragment_game_hub) {
    private val repo by lazy {
        GameProgressRepository(requireContext().applicationContext)
    }
    private lateinit var adapter: GameAdapter
    private var isLaunching = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        val recycler = view.findViewById<RecyclerView>(R.id.gameRecycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = GameAdapter { game ->
            handleGameClick(game)
        }
        recycler.adapter = adapter
        val baseGames = listOf(
            GameInfo(
                id = "memory_match",
                title = "Memory Match",
                description = "Find and match all pairs.",
                iconRes = R.drawable.ic_memory,
                status = GameStatus.NOT_STARTED
            ),
            GameInfo(
                id = "word_search",
                title = "Word Search",
                description = "Find hidden words in the grid.",
                iconRes = R.drawable.ic_word_search,
                status = GameStatus.NOT_STARTED
            )
        )
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repo.observeTodayProgress().collect { map ->
                    val updated = baseGames.map { game ->
                        val entity = map[game.id]
                        val status =
                            entity?.status?.let { GameStatus.valueOf(it) } ?: GameStatus.NOT_STARTED
                        game.copy(status = status)
                    }
                    adapter.submitList(updated)
                }
            }
        }
    }
    private fun handleGameClick(game: GameInfo){
        viewLifecycleOwner.lifecycleScope.launch {
            val progress = repo.getTodayProgress(game.id)
            android.util.Log.d("GameHub","progress=$progress")
            android.util.Log.d("GameHub", "savedState length=${progress?.savedStateJson?.length}")
            val hasSavedState = !progress?.savedStateJson.isNullOrBlank()
            val isInProgress = progress?.status == GameStatus.IN_PROGRESS.name
            if(isInProgress && hasSavedState){
                showResumeDialog(game, progress!!.difficulty)
            } else {
                showDifficultyDialog(game)
            }
        }
    }
    private fun showDifficultyDialog(game: GameInfo) {
        val options = arrayOf("Easy", "Medium", "Hard")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Choose difficulty")
            .setItems(options) { _, which ->
                val difficulty = when (which) {
                    0 -> Difficulty.EASY
                    1 -> Difficulty.MEDIUM
                    else -> Difficulty.HARD
                }
                launchGame(game, difficulty)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun showResumeDialog(game: GameInfo, savedDifficulty: String){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Resume ${game.title}?")
            .setMessage("You have a a in progress for today.")
            .setPositiveButton("Resume"){_,_ ->
                val difficulty = Difficulty.valueOf(savedDifficulty)
                launchGame(game, difficulty)
            }
            .setNegativeButton("Start New"){ _,_ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repo.clearState(game.id)
                    showDifficultyDialog(game)
                }
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun launchGame(game: GameInfo, difficulty: Difficulty) {
        if (isLaunching) return
        isLaunching = true
        val navController = findNavController()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                repo.markInProgress(game.id, difficulty)

                when (game.id) {
                    "memory_match" -> {
                        val action = GameHubFragmentDirections
                            .actionGameHubToMemoryMatch(difficulty.name)
                        navController.navigate(action)
                    }

                    "word_search" -> {
                        val action = GameHubFragmentDirections
                            .actionGameHubToWordSearch(difficulty.name)
                        navController.navigate(action)
                    }
                    else -> Snackbar.make(requireView(), "Game not implemented yet", Snackbar.LENGTH_SHORT).show()
                }
            } finally {
                isLaunching = false
            }
        }
    }
}