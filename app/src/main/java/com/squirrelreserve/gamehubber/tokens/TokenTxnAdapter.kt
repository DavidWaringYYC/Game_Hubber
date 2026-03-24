package com.squirrelreserve.gamehubber.tokens

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squirrelreserve.gamehubber.R
import com.squirrelreserve.gamehubber.data.db.TokenTxnEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TokenTxnAdapter : RecyclerView.Adapter<TokenTxnAdapter.VH>() {
    private var items: List<TokenTxnEntity> = emptyList()
    fun submit(list: List<TokenTxnEntity>){
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_token_txn, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }
    class VH(itemView: View): RecyclerView.ViewHolder(itemView){
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvReason: TextView = itemView.findViewById(R.id.tvReason)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvMeta: TextView = itemView.findViewById(R.id.tvMeta)
        private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fun bind(txn: TokenTxnEntity){
            val ctx = itemView.context
            val amount = txn.amount
            val amountText = if(amount >=0) "+$amount" else amount.toString()
            tvAmount.text = amountText
            val colorRes = if(amount >= 0) android.R.color.holo_green_dark else android.R.color.holo_red_dark
            tvAmount.setTextColor(ContextCompat.getColor(ctx, colorRes))
            tvReason.text = txn.reason.toString()
            val d = Date(txn.updatedAt)
            tvTime.text = timeFmt.format(d)
            val game = txn.gameId ?: "-"
            val dateKey = txn.dateKey ?: dateFmt.format(d)
            val txtMeta = "$game • $dateKey"
            tvMeta.text = txtMeta
        }
    }
}