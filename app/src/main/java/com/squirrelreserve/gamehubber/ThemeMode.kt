package com.squirrelreserve.gamehubber

import androidx.appcompat.app.AppCompatDelegate

enum class ThemeMode (val prefValue: String){
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");
    fun toNightMode(): Int = when (this){
        SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        DARK -> AppCompatDelegate.MODE_NIGHT_YES
    }
    companion object{
        fun fromPref(value: String?): ThemeMode = when (value) {
            LIGHT.prefValue -> ThemeMode.LIGHT
            DARK.prefValue -> DARK
            else -> SYSTEM
        }
    }
}