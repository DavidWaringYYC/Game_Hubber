package com.squirrelreserve.gamehubber

import android.app.Application
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class GameHubberApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val store = SettingsStore(applicationContext)
        val enabled = runBlocking { store.dynamicColorFlow.first() }
        if (enabled){
           DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }
}