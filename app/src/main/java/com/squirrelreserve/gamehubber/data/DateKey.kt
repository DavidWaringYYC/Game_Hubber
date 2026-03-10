package com.squirrelreserve.gamehubber.data

import java.time.LocalDate

object DateKey {
    fun today(): String = LocalDate.now().toString()
}