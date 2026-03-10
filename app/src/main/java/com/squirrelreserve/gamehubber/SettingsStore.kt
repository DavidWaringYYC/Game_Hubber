package com.squirrelreserve.gamehubber

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val  Context.dataStore by preferencesDataStore(name = "settings")
class SettingsStore(private val context: Context){
    private object Keys{
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }
    val themeModeFlow: Flow<ThemeMode> =
        context.dataStore.data.map{ prefs ->
            ThemeMode.fromPref(prefs[Keys.THEME_MODE])
        }
    val dynamicColorFlow: Flow<Boolean> =
        context.dataStore.data.map{ prefs ->
            prefs[Keys.DYNAMIC_COLOR] ?: true
        }
    suspend fun setThemeMode(mode: ThemeMode){
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.prefValue
        }
    }
    suspend fun setDynamicColor(enabled: Boolean){
        context.dataStore.edit { prefs ->
            prefs[Keys.DYNAMIC_COLOR] = enabled
        }
    }

}