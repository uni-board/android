package com.uniboard.board.domain

import androidx.compose.runtime.Immutable
import kotlinx.serialization.json.JsonElement

@Immutable
sealed interface UObjectUpdate {
    data class Add(val obj: UObject) : UObjectUpdate
    data class Modify(val diff: Map<String, JsonElement>) : UObjectUpdate
    data class Delete(val id: String) : UObjectUpdate
}