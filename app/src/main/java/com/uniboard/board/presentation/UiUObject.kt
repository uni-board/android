package com.uniboard.board.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import com.uniboard.board.domain.UObject
import com.uniboard.core.presentation.components.toDp
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

@Immutable
data class UiUObject(
    val id: String,
    val type: String,
    val top: Int = 0,
    val left: Int = 0,
    val width: Int? = null,
    val height: Int? = null,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val angle: Float = 0f,
    val editable: Boolean = false,
    val baseUrl: String = "",
    val state: Map<String, JsonElement>,
    val localState: Map<String, Any> = mapOf()
)

fun UiUObject.size(): Size? =
    if (width != null && height != null) Size(width.toFloat(), height.toFloat()) else null

fun UiUObject.dpSize(density: Density): DpSize? =
    size()?.let { DpSize(density.toDp(it.width), density.toDp(it.height)) }

fun UObject.toUiUObject(editable: Boolean = false, baseUrl: String = "") =
    UiUObject(
        id = id,
        type = type,
        top = state["top"]?.jsonPrimitive?.content?.toFloat()?.toInt() ?: 0,
        left = state["left"]?.jsonPrimitive?.content?.toFloat()?.toInt() ?: 0,
        width = state["width"]?.jsonPrimitive?.content?.toFloat()?.toInt(),
        height = state["height"]?.jsonPrimitive?.content?.toFloat()?.toInt(),
        scaleX = state["scaleX"]?.jsonPrimitive?.content?.toFloat() ?: 1f,
        scaleY = state["scaleY"]?.jsonPrimitive?.content?.toFloat() ?: 1f,
        angle = state["angle"]?.jsonPrimitive?.content?.toFloat() ?: 0f,
        editable = editable,
        baseUrl = baseUrl,
        state = state
    )

fun UiUObject.toUObject() =
    UObject(
        id = id,
        type = type,
        state = state
    )
