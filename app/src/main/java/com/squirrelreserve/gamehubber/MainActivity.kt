package com.squirrelreserve.gamehubber

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val store = SettingsStore(applicationContext)
        runBlocking {
            val mode = store.themeModeFlow.first()
            AppCompatDelegate.setDefaultNightMode(mode.toNightMode())
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}