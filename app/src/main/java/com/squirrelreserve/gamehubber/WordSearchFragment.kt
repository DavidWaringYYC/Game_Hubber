package com.squirrelreserve.gamehubber

import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.squirrelreserve.gamehubber.data.GameProgressRepository
import com.squirrelreserve.gamehubber.games.wordSearch.FoundLine
import com.squirrelreserve.gamehubber.games.wordSearch.LocalWordRepository
import com.squirrelreserve.gamehubber.games.wordSearch.WordGridAdapter
import com.squirrelreserve.gamehubber.games.wordSearch.WordListAdapter
import com.squirrelreserve.gamehubber.games.wordSearch.WordRepository
import com.squirrelreserve.gamehubber.games.wordSearch.WordSearchGenerator
import com.squirrelreserve.gamehubber.games.wordSearch.WordSearchOverlayView
import com.squirrelreserve.gamehubber.games.wordSearch.WordSearchState
import com.squirrelreserve.gamehubber.serialization.JsonProvider
import com.squirrelreserve.gamehubber.ui.setupToolbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.random.Random

class WordSearchFragment : Fragment(R.layout.fragment_word_search) {

    private val args: WordSearchFragmentArgs by navArgs()

    private val repo by lazy { GameProgressRepository(requireContext().applicationContext) }
    private val wordRepo: WordRepository = LocalWordRepository()

    private lateinit var rvGrid: RecyclerView
    private lateinit var overlay: WordSearchOverlayView
    private lateinit var gridAdapter: WordGridAdapter

    private lateinit var rvWords: RecyclerView
    private lateinit var wordAdapter: WordListAdapter

    private var state: WordSearchState? = null

    // drag state
    private var dragStart: Int? = null
    private var dragDir: Pair<Int, Int>? = null
    private var dragPath: List<Int> = emptyList()
    private var dragColor: Int? = null

    // timer
    private var sessionStartRealTime: Long = 0L
    private var timerJob: Job? = null

    // random color generation
    private val rng = Random(System.currentTimeMillis())
    private var lastColor: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val difficulty = Difficulty.valueOf(args.difficulty)
        view.setupToolbar(findNavController())

        overlay = view.findViewById(R.id.overlay)

        rvGrid = view.findViewById(R.id.rvGrid)
        rvGrid.layoutManager = GridLayoutManager(requireContext(), 8)
        gridAdapter = WordGridAdapter()
        rvGrid.adapter = gridAdapter

        rvGrid.post {
            val first = rvGrid.findViewHolderForAdapterPosition(0)?.itemView
            if (first != null) overlay.setCellSize(first.width.toFloat())
        }

        rvWords = view.findViewById(R.id.rvWords)
        rvWords.layoutManager = GridLayoutManager(requireContext(), 3)
        wordAdapter = WordListAdapter()
        rvWords.adapter = wordAdapter

        // Load/initialize state in ONE place (avoids race conditions)
        viewLifecycleOwner.lifecycleScope.launch {
            val progress = repo.getTodayProgress(GameIds.WORD_SEARCH)
            if (progress?.status == GameStatus.COMPLETED.name) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Completed today")
                    .setMessage("You already completed Word Search today. Come back tomorrow!")
                    .setPositiveButton("OK") { _, _ ->
                        if (isAdded) findNavController().navigateUp()
                    }
                    .show()
                return@launch
            }

            // Load saved state if exists, else new
            val json = repo.loadState(GameIds.WORD_SEARCH)
            state = if (!json.isNullOrBlank()) {
                JsonProvider.json.decodeFromString(WordSearchState.serializer(), json)
            } else {
                createNewGameState(difficulty)
            }

            sessionStartRealTime = SystemClock.elapsedRealtime()
            render(view)
            rvGrid.post{rebuildOverlayFromState()}
            startTimer(view)
        }

        rvGrid.setOnTouchListener { _, event ->
            handleGridTouch(event, view)
            true
        }
    }

    private suspend fun createNewGameState(difficulty: Difficulty): WordSearchState {
        val baseWords = wordRepo.getWords(difficulty)
        val rows = 8
        val cols = 8
        repeat(10){ attempt ->
            val shuffledWords = baseWords.shuffled(Random(System.nanoTime()))
            try{
                val grid = WordSearchGenerator.generate(rows = rows, cols = cols, words = shuffledWords, seed = System.nanoTime())
                return WordSearchState(difficulty = difficulty.name, grid = grid, words = shuffledWords)
            } catch (e: IllegalStateException){
                //try again
            }
        }
        return createFallBackGameState(difficulty, baseWords)
    }

    private suspend fun createFallBackGameState(
        difficulty: Difficulty,
        words: List<String>
    ): WordSearchState {
        val reducedWords = words.shuffled().take(maxOf(5, words.size * 2 /3))
        val grid = WordSearchGenerator.generate(rows = 8, cols = 8, words = reducedWords, seed = System.nanoTime())
        return WordSearchState(difficulty = difficulty.name, grid = grid, words = reducedWords)
    }

    private fun handleGridTouch(event: MotionEvent, rootView: View) {
        val s = state ?: return
        val cols = s.cols

        val pos = rvGrid.findChildViewUnder(event.x, event.y)?.let { rvGrid.getChildAdapterPosition(it) }
        if (pos == null || pos == RecyclerView.NO_POSITION) {
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) clearDrag(rootView)
            return
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dragStart = pos
                dragDir = null
                dragPath = listOf(pos)
                dragColor = nextSelectionColor()

                cellCenter(pos)?.let { (x, y) ->
                    overlay.setDragLine(x, y, x, y, dragColor!!)
                }
                render(rootView)
            }

            MotionEvent.ACTION_MOVE -> {
                val start = dragStart ?: return
                if (pos == start) return

                val sr = start / cols
                val sc = start % cols
                val pr = pos / cols
                val pc = pos % cols

                val dr = pr - sr
                val dc = pc - sc

                val stepR = dr.coerceIn(-1, 1)
                val stepC = dc.coerceIn(-1, 1)

                if (stepR == 0 && stepC == 0) return
                if (abs(dr) != abs(dc) && dr != 0 && dc != 0) return // only straight/diag

                if (dragDir == null) dragDir = stepR to stepC

                val (dirR, dirC) = dragDir ?: return
                val newPath = buildPath(start, pos, cols, dirR, dirC)

                if (newPath.isNotEmpty()) {
                    dragPath = newPath

                    val end = dragPath.last()
                    val p1 = cellCenter(start)
                    val p2 = cellCenter(end)
                    if (p1 != null && p2 != null) {
                        overlay.setDragLine(p1.first, p1.second, p2.first, p2.second, dragColor!!)
                    }

                    render(rootView)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                finalizeSelection(rootView)
            }
        }
    }

    private fun buildPath(start: Int, end: Int, cols: Int, dirR: Int, dirC: Int): List<Int> {
        val s = state ?: return emptyList()

        val sr = start / cols
        val sc = start % cols
        val er = end / cols
        val ec = end % cols

        var r = sr
        var c = sc

        val path = mutableListOf<Int>()
        path.add(start)

        while (r != er || c != ec) {
            r += dirR
            c += dirC
            if (r !in 0 until s.rows || c !in 0 until s.cols) return emptyList()
            path.add(r * cols + c)
        }
        return path
    }

    private fun finalizeSelection(rootView: View) {
        val s = state ?: return
        if (dragPath.size < 2) {
            clearDrag(rootView)
            return
        }

        val selected = dragPath.map { s.grid[it] }.joinToString(separator = "").uppercase()
        val reversed = selected.reversed()
        val wordSet = s.words.map { it.uppercase() }.toSet()

        val matchedWord = when {
            wordSet.contains(selected) -> selected
            wordSet.contains(reversed) -> reversed
            else -> null
        }

        if (matchedWord != null && !s.found.contains(matchedWord)) {
            val wordColor = dragColor ?: nextSelectionColor()

            // Persist found word + color
            val newFound = s.found + matchedWord
            val newWordColors = s.wordColors.toMutableMap()
            newWordColors[matchedWord] = wordColor

            // Draw permanent highlight line
            val start = dragPath.first()
            val end = dragPath.last()
            val newFoundLines = s.foundLines + FoundLine(
                startIndex = start,
                endIndex = end,
                color = wordColor
            )
            state = s.copy(
                found = newFound,
                wordColors = newWordColors,
                foundLines = newFoundLines
            )
            val p1 = cellCenter(start)
            val p2 = cellCenter(end)
            if (p1 != null && p2 != null) {
                overlay.addFoundLine(p1.first, p1.second, p2.first, p2.second, wordColor)
            }

            render(rootView)

            if (newFound.size == s.words.size) {
                onGameCompleted()
            }
        }

        clearDrag(rootView)
    }

    private fun clearDrag(rootView: View) {
        dragStart = null
        dragDir = null
        dragPath = emptyList()
        dragColor = null
        overlay.clearDragLine()
        render(rootView)
    }

    private fun render(view: View) {
        val s = state ?: return
        view.findViewById<TextView>(R.id.tvTimer).text =
            "Time: ${formatTime(s.elapsedMs + currentSessionMs())}"

        // Grid still renders letters, but no longer uses cellColors for highlighting
        gridAdapter.submit(s.rows, s.cols, s.grid, emptyMap(), emptySet(), null)

        wordAdapter.submit(s.words, s.found, s.wordColors)
    }

    private fun startTimer(view: View) {
        timerJob?.cancel()
        timerJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    render(view)
                    delay(1000)
                }
            }
        }
    }

    private fun currentSessionMs(): Long =
        if (sessionStartRealTime == 0L) 0L else (SystemClock.elapsedRealtime() - sessionStartRealTime)

    private fun onGameCompleted() {
        timerJob?.cancel()

        lifecycleScope.launch {
            val earned = repo.markCompleted(GameIds.WORD_SEARCH)
            repo.clearState(GameIds.WORD_SEARCH)
            if (earned > 0) {
                Snackbar.make(requireView(), "+$earned Tokens earned!", Snackbar.LENGTH_LONG).show()
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Word Search Completed!")
                .setMessage("Congratulations! You are done for today.")
                .setCancelable(false)
                .setPositiveButton("Back to Hub") { _, _ ->
                    if (isAdded) findNavController().navigateUp()
                }
                .show()
        }
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = (ms / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    private fun cellCenter(index: Int): Pair<Float, Float>? {
        val vh = rvGrid.findViewHolderForAdapterPosition(index) ?: return null
        val v = vh.itemView
        return (v.x + v.width / 2f) to (v.y + v.height / 2f)
    }

    private fun nextSelectionColor(): Int {
        val v = requireView()
        val base = listOf(
            MaterialColors.getColor(v, androidx.appcompat.R.attr.colorPrimary),
            MaterialColors.getColor(v, com.google.android.material.R.attr.colorSecondary),
            MaterialColors.getColor(v, com.google.android.material.R.attr.colorTertiary)
        ).random(rng)

        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(base, hsl)
        hsl[0] = (hsl[0] + rng.nextFloat() * 180f) % 360f
        hsl[1] = 0.70f
        hsl[2] = 0.55f

        var color = ColorUtils.HSLToColor(hsl)
        color = ColorUtils.setAlphaComponent(color, 0xFF)

        if (lastColor != null && color == lastColor) {
            hsl[0] = (hsl[0] + 45f) % 360f
            color = ColorUtils.setAlphaComponent(ColorUtils.HSLToColor(hsl), 0xFF)
        }

        lastColor = color
        return color
    }
    private fun rebuildOverlayFromState(){
        val s = state ?: return
        val overlayLines = s.foundLines.mapNotNull { fl ->
            val p1 = cellCenter(fl.startIndex)
            val p2 = cellCenter(fl.endIndex)
            if(p1 != null && p2 != null){
                WordSearchOverlayView.Line(p1.first, p1.second, p2.first, p2.second, fl.color)
            } else null
        }
        overlay.setFoundLines(overlayLines)
    }
    override fun onStop() {
        super.onStop()

        val s = state ?: return
        val updated = s.copy(elapsedMs = s.elapsedMs + currentSessionMs())
        state = updated

        // stop timer
        timerJob?.cancel()
        timerJob = null

        val difficulty = Difficulty.valueOf(args.difficulty)

        lifecycleScope.launch {
            withContext(NonCancellable) {
                val json = JsonProvider.json.encodeToString(WordSearchState.serializer(), updated)
                repo.saveState(GameIds.WORD_SEARCH, difficulty, json)
            }
        }
    }
}