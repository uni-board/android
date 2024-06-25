package com.uniboard.domain

import androidx.compose.runtime.Immutable
import kotlinx.serialization.json.JsonElement

@Immutable
data class UObject(
    val id: String,
    val type: String,
    val state: Map<String, JsonElement>
)