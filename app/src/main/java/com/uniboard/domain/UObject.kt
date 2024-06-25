package com.uniboard.domain

import kotlinx.serialization.json.JsonElement

data class UObject(
    val id: String,
    val type: String,
    val state: Map<String, JsonElement>
)