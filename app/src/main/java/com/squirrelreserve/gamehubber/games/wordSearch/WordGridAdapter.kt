package com.squirrelreserve.gamehubber.games.wordSearch

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.squirrelreserve.gamehubber.R

class WordGridAdapter : RecyclerView.Adapter<WordGridAdapter.VH>() {
    private var rows = 8
    private var cols = 8
    private var grid: String = ""
    private var foundColors: Map<Int, Int> = emptyMap()
    private var dragPath: Set<Int> = emptySet()
    private var dragColor: Int? = null

    fun submit(
        rows: Int,
        cols: Int,
        grid: String,
        foundColors: Map<Int, Int>,
        dragPath: Set<Int>,
        dragColor: Int?
    ){
        this.rows = rows
        this.cols = cols
        this.grid = grid
        this.foundColors = foundColors
        this.dragPath = dragPath
        this.dragColor = dragColor
        notifyDataSetChanged()
    }
    override fun getItemCount() = rows * cols
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_word_cell, parent, false)
        return VH(view as MaterialCardView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ch = grid.getOrNull(position) ?: ' '
        holder.bind(ch, position, foundColors[position], dragPath.contains(position), dragColor)
    }
    class VH(private val root: MaterialCardView) : RecyclerView.ViewHolder(root){
        private val tv: TextView = root.findViewById(R.id.tvLetter)
        fun bind(letter: Char, index: Int, foundColor: Int?,isDragging: Boolean, dragColor: Int?){
            tv.text = letter.toString()
            val ctx = root.context
            val surface = MaterialColors.getColor(root, com.google.android.material.R.attr.colorSurface)
            val outline = MaterialColors.getColor(root, com.google.android.material.R.attr.colorOutline)
            root.strokeColor = outline
            root.strokeWidth = 1
            when {
                foundColor != null ->{
                    root.setCardBackgroundColor(foundColor)
                    tv.setTextColor(MaterialColors.getColor(root, com.google.android.material.R.attr.colorOnPrimary))
                }
                isDragging && dragColor != null ->{
                    root.setCardBackgroundColor(dragColor)
                    tv.setTextColor(MaterialColors.getColor(root, com.google.android.material.R.attr.colorOnPrimary))
                }
                else -> {
                    root.setCardBackgroundColor(surface)
                    tv.setTextColor(MaterialColors.getColor(root, com.google.android.material.R.attr.colorOnSurface))
                }
            }
        }
    }
}