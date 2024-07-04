package com.uniboard.board.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.uniboard.board.domain.RemoteObject
import com.uniboard.board.domain.UObject
import com.uniboard.board.presentation.BoardToolModeState
import com.uniboard.board.presentation.ShapeType
import com.uniboard.board.presentation.util.asCSSString
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.abs


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomPathObjectCreator(
    mode: BoardToolModeState,
    onCreate: (UObject) -> Unit,
    modifier: Modifier = Modifier
) {
    val updatedMode by rememberUpdatedState(mode)
    var end by remember { mutableStateOf(Offset.Zero) }
    var start by remember { mutableStateOf(Offset.Zero) }
    val state = rememberDraggable2DState {
        end += it
    }
    val type = remember(updatedMode) {
        updatedMode.state["type"] as? ShapeType
    }
    val fill = remember(updatedMode) {
        updatedMode.state["fill"] as? Boolean
    }
    val color = remember(updatedMode) {
        updatedMode.state["stroke"] as? Color
    }
    val strokeWidth = remember(updatedMode) {
        updatedMode.state["strokeWidth"] as? Float
    }
    Canvas(modifier.draggable2D(state, onDragStarted = {
        start = it
        end = it
    }, onDragStopped = {
        onCreate(createUObj(start, end, updatedMode))
    })) {
        val path = Path()
        val size = sizeFromOffsets(start, end).normalize(type)
        when (type) {
            ShapeType.Triangle -> path.drawTriangle(size)
            ShapeType.Square -> path.drawRect(size)
            ShapeType.Circle, ShapeType.Oval -> path.drawOval(size)
            ShapeType.Line -> path.drawLine(start.x, start.y, end.x, end.y)
            null -> {}
        }
        path.translate(Offset(minOf(start.x, end.x), minOf(start.y, end.y)))
        val style = if (fill == true) Fill else Stroke(width = strokeWidth ?: 10f)
        if (color != null) drawPath(path, color = color, style = style)
    }
}

private fun Size.normalize(type: ShapeType?) =
    if (type == ShapeType.Circle) Size(maxDimension, maxDimension)
    else this

private fun sizeFromOffsets(
    start: Offset,
    end: Offset
) = Size(abs(start.x - end.x), abs(start.y - end.y))

private fun createUObj(start: Offset, end: Offset, mode: BoardToolModeState): UObject {
    val type = mode.state["type"] as? ShapeType
    val color = mode.state["color"] as? Color
    val fill = mode.state["fill"] as? Boolean
    val strokeWidth = mode.state["strokeWidth"] as? Float
    return RemoteObject.create(requireNotNull(type?.remoteName)) {
        this["top"] = JsonPrimitive(minOf(start.y, end.y))
        this["left"] = JsonPrimitive(minOf(start.x, end.x))
        val size = sizeFromOffsets(start, end).normalize(type)
        this["width"] = JsonPrimitive(size.width)
        this["height"] = JsonPrimitive(size.height)

        if (type == ShapeType.Circle || type == ShapeType.Oval) {
            this["rx"] = JsonPrimitive(size.width / 2)
            this["ry"] = JsonPrimitive(size.height / 2)
        }
        val color = requireNotNull(color?.asCSSString())
        if (type != ShapeType.Line) {
            val fillColor =
                if (fill == true) color else "transparent"
            this["fill"] = JsonPrimitive(fillColor)
        } else {
            this["x1"] = JsonPrimitive(start.x)
            this["y1"] = JsonPrimitive(start.y)
            this["x2"] = JsonPrimitive(end.x)
            this["y2"] = JsonPrimitive(end.y)
        }
        this["stroke"] = JsonPrimitive(color)
        this["strokeWidth"] = JsonPrimitive(requireNotNull(strokeWidth))
    }
}