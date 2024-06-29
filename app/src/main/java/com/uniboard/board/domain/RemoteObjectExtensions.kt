package com.uniboard.board.domain

import com.uniboard.util.diffWith
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.format.KotlinxSerialization.asJsonObject
import org.http4k.format.KotlinxSerialization.asJsonValue
import java.util.UUID

object RemoteObject {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun toUObjectList(jsonArray: JsonArray): List<UObject> {
        return jsonArray.map { toUObject(it.jsonObject) }
    }

    fun toUObject(jsonObject: JsonObject): UObject {
        val data =
            checkNotNull(jsonObject["uniboardData"]?.jsonObject) { "uniboard-data is null: $jsonObject" }
        return UObject(
            id = data["id"]?.jsonPrimitive?.content ?: error("id is null"),
            type = data["type"]?.jsonPrimitive?.content ?: error("id is null"),
            state = jsonObject
        )
    }

    fun createDiff(oldObj: UObject, obj: UObject): Map<String, JsonElement> {
        return obj.state.diffWith(oldObj.state) + mapOf(
            "uniboardData" to (obj.state["uniboardData"] ?: JsonNull),
            "selectable" to JsonPrimitive(true)
        )
    }

    fun create(type: String, block: MutableMap<String, JsonElement>.() -> Unit): UObject {
        val id = UUID.randomUUID()
        val state = mutableMapOf<String, JsonElement>()
        state.apply {
            val uniboardData = JsonObject(
                mapOf(
                    "id" to id.toString().asJsonValue(),
                    "type" to type.asJsonValue(),
                    "persistedOnServer" to false.asJsonValue(),
                )
            )
            this["uniboardData"] = uniboardData
            this["type"] = JsonPrimitive(if (type.startsWith("uniboard")) "group" else type)
            this["version"] = "5.3.0".asJsonValue()
        }
        state.block()
        return UObject(
            id = id.toString(),
            type = type,
            state = state
        )
    }

    fun toUObjectFromDiff(oldObj: UObject, diff: Map<String, JsonElement>): UObject {
        return UObject(
            id = oldObj.id,
            type = oldObj.type,
            state = oldObj.state + diff
        )
    }

    fun idFromDiff(diff: Map<String, JsonElement>): String {
        return diff["uniboardData"]?.jsonObject?.get("id")?.jsonPrimitive?.content
            ?: error("id is null")
    }

    fun toUObject(string: String): UObject {
        val decoded = json.decodeFromString<JsonObject>(string)
        return toUObject(decoded)
    }
}