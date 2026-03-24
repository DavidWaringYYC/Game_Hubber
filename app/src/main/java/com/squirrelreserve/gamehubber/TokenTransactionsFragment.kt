package com.squirrelreserve.gamehubber

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squirrelreserve.gamehubber.data.db.AppDatabase
import com.squirrelreserve.gamehubber.tokens.TokenTxnAdapter
import com.squirrelreserve.gamehubber.ui.setupToolbar
import kotlinx.coroutines.launch

class TokenTransactionsFragment: Fragment(R.layout.fragment_token_transactions) {
    private lateinit var adapter: TokenTxnAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setupToolbar(findNavController())
        val rv = view.findViewById<RecyclerView>(R.id.rvTxns)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)
        adapter = TokenTxnAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
        val db = AppDatabase.get(requireContext().applicationContext)
        val txnDao = db.tokenTxnDao()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                txnDao.observeTxns().collect { list ->
                    adapter.submit(list)
                    tvEmpty.visibility = if(list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }
}