package com.uniboard.board.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathSegment
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.copy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import com.uniboard.board.domain.RemoteObject
import com.uniboard.board.domain.UObject
import com.uniboard.board.presentation.BoardToolModeState
import com.uniboard.board.presentation.UiUObject
import com.uniboard.board.presentation.UiUObjectApi
import com.uniboard.board.presentation.content
import com.uniboard.board.presentation.copyWith
import com.uniboard.board.presentation.icon
import com.uniboard.board.presentation.util.asCSSString
import com.uniboard.board.presentation.util.parseAsRGBAColor
import com.uniboard.core.presentation.rememberState
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.format.KotlinxSerialization.asJsonArray
import org.http4k.format.KotlinxSerialization.asJsonValue

fun PathObject() = UiUObjectApi {
    type { it == "path" }
    content { obj, modifier ->
        val path = remember(obj) {
            createPathFor(obj)
        }
        val strokeWidth = remember(obj) {
            obj.state["strokeWidth"]?.jsonPrimitive?.content?.toFloat() ?: 100f
        }
        val cap = remember(obj) {
            val str = obj.state["strokeLineCap"]?.jsonPrimitive?.content ?: "round"
            when (str) {
                "round" -> StrokeCap.Round
                "square" -> StrokeCap.Square
                else -> StrokeCap.Butt
            }
        }
        val join = remember(obj) {
            val str = obj.state["strokeLineJoin"]?.jsonPrimitive?.content ?: "round"
            when (str) {
                "round" -> StrokeJoin.Round
                "bevel" -> StrokeJoin.Bevel
                else -> StrokeJoin.Miter
            }
        }
        val color = remember(obj) {
            obj.state["stroke"]?.jsonPrimitive?.content?.parseAsRGBAColor() ?: Color.Green
        }
        Box(
            modifier
                .drawBehind {
                    drawPath(
                        path, color,
                        style = Stroke(
                            width = strokeWidth,
                            cap = cap,
                            join = join
                        )
                    )
                })
    }

    creator { mode, onCreate, modifier ->
        PathObjectCreator(mode, onCreate, modifier)
    }
    toolbar("path") {
        options { mode, onSelect, modifier ->
            PenOptions(mode, onSelect, modifier)
        }
        icon(Icons.Default.Draw)
    }
}

@Composable
fun PathObjectCreator(
    mode: BoardToolModeState,
    onCreate: (UObject) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = remember(mode) { mode.state["color"] as? Color ?: Color.Black }
    val width = remember(mode) { mode.state["width"] as? Float ?: 5f }
    var path by rememberState { Path() }
    val drawModifier = modifier
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown().also {
                    path.moveTo(it.position.x, it.position.y)
                    path = path.copy()
                }
                do {
                    val event: PointerEvent = awaitPointerEvent()
                    event.changes
                        .forEachIndexed { _, pointerInputChange ->
                            if (pointerInputChange.positionChange() != Offset.Zero) pointerInputChange.consume()
                        }
                    val position = event.changes.first().position
                    path.lineTo(position.x, position.y)
                    path = path.copy()
                } while (event.changes.any { it.pressed })

                onCreate(path.createUObject(color, width))
            }
        }

    Canvas(modifier = drawModifier) {
        println(path)
        drawPath(
            color = color,
            path = path,
            style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun PenOptions(
    selectedMode: BoardToolModeState,
    onSelect: (BoardToolModeState) -> Unit,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues()
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val color = remember(selectedMode) {
            selectedMode.state["color"] as? Color ?: Color.Black
        }
        val width = remember(selectedMode) {
            selectedMode.state["width"] as? Float ?: 5f
        }
        ColorCarousel(
            color,
            onSelect = { onSelect(selectedMode.copyWith { this["color"] = it }) }, padding = padding
        )
        Slider(width, onValueChange = {
            onSelect(selectedMode.copyWith { this["width"] = it })
        }, valueRange = 5f..60f, modifier = Modifier.padding(padding))
    }
}

private fun Path.createUObject(color: Color, width: Float): UObject {
    return RemoteObject.create("path") {
        this["path"] = createList().asJsonArray()
        this["top"] = JsonPrimitive(getBounds().top)
        this["left"] = JsonPrimitive(getBounds().left)
        this["width"] = JsonPrimitive(getBounds().right - getBounds().left)
        this["height"] = JsonPrimitive(getBounds().bottom - getBounds().top)
        this["fill"] = JsonNull
        this["stroke"] = color.asCSSString().asJsonValue()
        this["strokeWidth"] = JsonPrimitive(width)
        this["strokeLineCap"] = "round".asJsonValue()
        this["strokeLineJoin"] = "round".asJsonValue()
        this["strokeLineLimit"] = JsonPrimitive(10)
    }
}

private fun Path.createList(): List<JsonElement> {
    return iterator().asSequence()
        .map {
            it.asSVGPath().asJsonArray()
        }.toList()
}

private fun PathSegment.asSVGPath(): List<JsonPrimitive> {
    return listOf(
        when (type) {
            PathSegment.Type.Move -> "M"
            PathSegment.Type.Line -> "L"
            PathSegment.Type.Quadratic -> "Q"
            PathSegment.Type.Conic -> throw IllegalArgumentException(this.toString())
            PathSegment.Type.Cubic -> "C"
            PathSegment.Type.Close -> "Z"
            PathSegment.Type.Done -> "Z"
        }.asJsonValue()
    ) + points.map { JsonPrimitive(it) }
}

private fun createPathFor(obj: UiUObject): Path {
    val pathArray = requireNotNull(obj.state["path"]?.jsonArray) { "Path not found: $obj" }
        .map { array -> array.jsonArray.map { it.jsonPrimitive.content } }
    val initialOffset = Offset(obj.left.toFloat(), obj.top.toFloat())
    val path = createPath(pathArray, initialOffset)
    val bounds = path.getBounds()
    val offset = Offset(bounds.left, bounds.top)
    path.translate(-offset)
    return path
}

private fun createPath(pathArray: List<List<Any>>, offset: Offset): Path {
    val pathString = pathArray.joinToString(" ") { element ->
        element.mapIndexed { index, content ->
            if (index > 0) {
                val value = content.toString().toFloat()
                if (index % 2 == 0) value - offset.y else value - offset.x
            } else content
        }.joinToString(" ")
    }
    return addPathNodes(pathString).toPath()
}
