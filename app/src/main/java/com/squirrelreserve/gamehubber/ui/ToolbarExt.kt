package com.squirrelreserve.gamehubber.ui
import android.view.View
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.squirrelreserve.gamehubber.R

fun View.setupToolbar(
    navController: NavController,
    @IdRes topLevelDestination: Int = R.id.gameHubFragment,
    @IdRes toolbarId: Int = R.id.toolbar
): MaterialToolbar{
    val toolbar = findViewById<MaterialToolbar>(toolbarId)
    val config = AppBarConfiguration(setOf(topLevelDestination))
    toolbar.setupWithNavController(navController, config)
    return toolbar
}