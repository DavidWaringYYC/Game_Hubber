package com.squirrelreserve.gamehubber

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.google.android.material.imageview.ShapeableImageView

class GameAdapter (
    private val onClick: (GameInfo) -> Unit
): ListAdapter<GameInfo, GameAdapter.GameViewHolder>(Diff){
    object Diff : DiffUtil.ItemCallback<GameInfo>(){
        override fun areItemsTheSame(oldItem: GameInfo, newItem: GameInfo) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GameInfo, newItem: GameInfo) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game_card, parent, false)
        return GameViewHolder(view as ViewGroup, onClick)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    class GameViewHolder(
        root: ViewGroup,
        private val onClick: (GameInfo) -> Unit
    ): RecyclerView.ViewHolder(root){
        private val icon : ShapeableImageView = root.findViewById(R.id.icon)
        private val title: TextView = root.findViewById(R.id.title)
        private val description: TextView = root.findViewById(R.id.description)
        private val statusChip: Chip = root.findViewById(R.id.statusChip)
        fun bind(item: GameInfo) {
            icon.setImageResource(item.iconRes)
            title.text = item.title
            description.text = item.description
            statusChip.text = when (item.status){
                GameStatus.NOT_STARTED -> "Not Started"
                GameStatus.IN_PROGRESS -> "In Progress"
                GameStatus.COMPLETED -> "Completed"
            }
            val ctx = itemView.context
            val tintColor = when (item.status){
                GameStatus.NOT_STARTED -> MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOutline)
                GameStatus.IN_PROGRESS -> MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnPrimary)
                GameStatus.COMPLETED -> MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorTertiary)
            }
            statusChip.chipBackgroundColor  = ColorStateList.valueOf(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorSurfaceContainerHighest))
            statusChip.chipStrokeWidth = 1f
            statusChip.chipStrokeColor = ColorStateList.valueOf(tintColor)
            statusChip.setTextColor(tintColor)
            itemView.setOnClickListener { onClick(item) }
        }
    }
}