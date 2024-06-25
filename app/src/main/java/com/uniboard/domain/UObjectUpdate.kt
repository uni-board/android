package com.uniboard.domain

import kotlinx.serialization.json.JsonElement

sealed interface UObjectUpdate {
    data class Add(val obj: UObject) : UObjectUpdate
    data class Modify(val diff: Map<String, JsonElement>) : UObjectUpdate
    data class Delete(val id: String) : UObjectUpdate
}