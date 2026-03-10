package com.squirrelreserve.gamehubber

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.squirrelreserve.gamehubber.data.GameProgressRepository
import com.squirrelreserve.gamehubber.games.memory.MemoryMatchState
import com.squirrelreserve.gamehubber.games.wordSearch.WordSearchState
import com.squirrelreserve.gamehubber.serialization.JsonProvider
import com.squirrelreserve.gamehubber.ui.setupToolbar
import kotlinx.coroutines.launch


class WordSearchFragment : Fragment(R.layout.fragment_word_search) {
    private val args: WordSearchFragmentArgs by navArgs()
    private val repo by lazy {
        GameProgressRepository(requireContext().applicationContext)
    }
    private var state: WordSearchState? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val difficulty = Difficulty.valueOf(args.difficulty)
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
        viewLifecycleOwner.lifecycleScope.launch {
            val json = repo.loadState("word_search")
            state = if (json != null) {
                JsonProvider.json.decodeFromString<WordSearchState>(json)
            } else {
                WordSearchState(difficulty = difficulty.name)
            }
            //TODO later: update UI based on restored state
        }
    }

    override fun onPause() {
        super.onPause()
        val currentState = state ?: return
        val difficulty = Difficulty.valueOf(args.difficulty)
        viewLifecycleOwner.lifecycleScope.launch {
            val json = JsonProvider.json.encodeToString(currentState)
            repo.saveState("word_search", difficulty, json)
        }
    }
}
