package com.squirrelreserve.gamehubber.ui
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.squirrelreserve.gamehubber.R
import com.squirrelreserve.gamehubber.data.TokenRepository
import com.squirrelreserve.gamehubber.data.db.AppDatabase
import kotlinx.coroutines.launch
import java.text.NumberFormat

fun View.setupToolbar(
    navController: NavController,
    @IdRes topLevelDestination: Int = R.id.gameHubFragment,
    @IdRes toolbarId: Int = R.id.toolbar,
    @MenuRes menuId: Int = R.menu.menu_game_hub,
    @IdRes settingsMenuItemId: Int = R.id.action_settings,
    @IdRes globalSettingsActionId: Int = R.id.action_global_to_settingsFragment
): MaterialToolbar{
    val toolbar = findViewById<MaterialToolbar>(toolbarId)
    val config = AppBarConfiguration(setOf(topLevelDestination))
    toolbar.setupWithNavController(navController, config)
    toolbar.menu.clear()
    toolbar.inflateMenu(menuId)
    bindTokensIfPresent(toolbar)
    toolbar.setOnMenuItemClickListener { item ->
        when (item.itemId){
            settingsMenuItemId -> {
                navController.navigate(globalSettingsActionId)
                true
            }
            else -> false
        }
    }
    return toolbar
}
private fun View.bindTokensIfPresent(toolbar: MaterialToolbar){
    val tokenItem = toolbar.menu.findItem(R.id.action_tokens) ?: return
    val chip = tokenItem.actionView?.findViewById<Chip>(R.id.chipTokens) ?: return
    chip.chipIcon = AppCompatResources.getDrawable(
        toolbar.context, R.drawable.ic_token
    )
    chip.text = "999"
    chip.isChipIconVisible = true
    val lifecycleOwner = findViewTreeLifecycleOwner() ?: return
    val db = AppDatabase.get(toolbar.context)
    val tokenRepo = TokenRepository(db,db.walletDao(), db.tokenTxnDao())
    val formatter = NumberFormat.getIntegerInstance()
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
            tokenRepo
                .observeBalance()
                .collect { balance ->
                    chip.text = formatter.format(balance)
                }
        }
    }
}