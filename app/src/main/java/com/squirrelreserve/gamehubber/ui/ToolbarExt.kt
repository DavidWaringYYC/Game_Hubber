package com.squirrelreserve.gamehubber.ui
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.squirrelreserve.gamehubber.R

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