package com.bobon.mypace.logger

import kotlinx.serialization.json.Json

object LogJson {
    val json = Json {
        encodeDefaults = false
        explicitNulls = false
        ignoreUnknownKeys = true
        classDiscriminator = "kind"
    }
}