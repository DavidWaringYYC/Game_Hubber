package com.squirrelreserve.gamehubber.serialization

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


object JsonProvider {
    val json: Json = Json{
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }
}