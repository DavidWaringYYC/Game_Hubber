package com.squirrelreserve.gamehubber.games.wordSearch

import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.contains
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.squirrelreserve.gamehubber.R

class WordListAdapter : RecyclerView.Adapter<WordListAdapter.VH>() {
    private var words: List<String> = emptyList()
    private var found: Set<String> = emptySet()
    private var wordColors: Map<String, Int> = emptyMap()

    fun submit(words: List<String>, found: Set<String>, wordColors: Map<String, Int>){
        this.words = words
        this.found = found
        this.wordColors = wordColors
        notifyDataSetChanged()
    }

    override fun getItemCount() = words.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordListAdapter.VH {
        val chip = LayoutInflater.from(parent.context).inflate(R.layout.item_word_chip, parent, false) as Chip
        return VH(chip)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(words[position], found.contains(words[position]), wordColors[words[position]])
    }
    class VH(private val chip: Chip): RecyclerView.ViewHolder(chip){
        fun bind(word: String, isFound: Boolean, color: Int?){
            chip.text = word
            if (isFound && color != null){
                chip.chipBackgroundColor = ColorStateList.valueOf(color)
                chip.setTextColor(MaterialColors.getColor(chip, com.google.android.material.R.attr.colorOnPrimary))
                chip.paintFlags = chip.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                chip.chipBackgroundColor = ColorStateList.valueOf(MaterialColors.getColor(chip, com.google.android.material.R.attr.colorSurfaceContainerHighest))
                chip.setTextColor(MaterialColors.getColor(chip, com.google.android.material.R.attr.colorOnSurface))
                chip.paintFlags = chip.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }
}