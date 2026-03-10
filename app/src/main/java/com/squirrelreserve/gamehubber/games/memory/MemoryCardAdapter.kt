package com.squirrelreserve.gamehubber.games.memory

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.imageview.ShapeableImageView
import com.squirrelreserve.gamehubber.R

class MemoryCardAdapter (
    private val onCardClick: (index: Int)-> Unit
) : RecyclerView.Adapter<MemoryCardAdapter.VH>(){
    private var cards: List<CardState> = emptyList()
    private var itemHeightPx: Int = 0
    private val icons = listOf(

        SymbolSpec(R.drawable.ic_lightning, Color.parseColor("#FBC02D")), // yellow
        SymbolSpec(R.drawable.ic_heart,     Color.parseColor("#E53935")), // red
        SymbolSpec(R.drawable.ic_leaf,      Color.parseColor("#43A047")), // green
        SymbolSpec(R.drawable.ic_moon,      Color.parseColor("#7E57C2")), // purple
        SymbolSpec(R.drawable.ic_star,      Color.parseColor("#F9A825")), // gold
        SymbolSpec(R.drawable.ic_cloud,     Color.parseColor("#42A5F5")), // blue
        SymbolSpec(R.drawable.ic_flower,    Color.parseColor("#EC407A")), // pink
        SymbolSpec(R.drawable.ic_diamond,   Color.parseColor("#26A69A")), // teal
        SymbolSpec(R.drawable.ic_music,     Color.parseColor("#5C6BC0")), // indigo
        SymbolSpec(R.drawable.ic_balloon,   Color.parseColor("#FB8C00")), // orange
        SymbolSpec(R.drawable.ic_sun,       Color.parseColor("#FFB300")), // amber
        SymbolSpec(R.drawable.ic_jigsaw,    Color.parseColor("#8D6E63"))  // brown

    )
    fun submit(cards: List<CardState>){
        this.cards = cards
        notifyDataSetChanged()
    }
    fun setItemSize(cellSizePx: Int){
        itemHeightPx = cellSizePx
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_memory_card, parent, false)
        return VH(view as MaterialCardView, onCardClick, icons)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(cards[position], position, itemHeightPx)
    }

    override fun getItemCount() = cards.size
    class VH(
        private val root: MaterialCardView,
        private val onCardClick: (Int) -> Unit,
        private val icons: List<SymbolSpec>
    ): RecyclerView.ViewHolder(root){
        private val imgIcon: ShapeableImageView = root.findViewById(R.id.imgIcon)
        private val imgBack: ShapeableImageView = root.findViewById(R.id.imgBack)
        fun bind(card: CardState, index: Int, heightPx: Int){
            if(heightPx > 0){
                root.layoutParams = root.layoutParams.apply {
                    height = heightPx
                }
            }
            val showFront = card.isFaceUp || card.isMatched
            val neutralTint = MaterialColors.getColor(root, com.google.android.material.R.attr.colorOnSurface)
            val matchedTint = MaterialColors.getColor(root, androidx.appcompat.R.attr.colorPrimary)
            imgIcon.isVisible = showFront
            imgBack.isVisible = !showFront
            if (showFront){
                val iconRes = icons[card.pairId % icons.size]
                val tintColor = if(card.isMatched) iconRes.matchedColor else neutralTint
                imgIcon.setImageResource(iconRes.iconRes)
                imgIcon.imageTintList = ColorStateList.valueOf(tintColor)
            } else {
                imgIcon.setImageDrawable(null)
            }
            root.isEnabled = !card.isMatched
            root.alpha = if (card.isMatched) 0.35f else 1f
            root.setOnClickListener {
                if(!card.isMatched) onCardClick(index)
            }
        }
    }
}