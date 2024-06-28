package com.uniboard.presentation.board.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.uniboard.domain.RemoteObject
import com.uniboard.domain.UObject
import com.uniboard.presentation.board.BoardToolMode
import com.uniboard.presentation.board.ShapeType
import com.uniboard.presentation.board.util.asCSSString
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.abs


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomPathObjectCreator(
    mode: BoardToolMode.Shape,
    onCreate: (UObject) -> Unit,
    modifier: Modifier = Modifier
) {
    var end by remember { mutableStateOf(Offset.Zero) }
    var start by remember { mutableStateOf(Offset.Zero) }
    val state = rememberDraggable2DState {
        end += it
    }
    Canvas(modifier.draggable2D(state, onDragStarted = {
        start = it
        end = it
    }, onDragStopped = {
        onCreate(createUObj(start, end, mode))
    })) {
        val path = Path()
        val size = sizeFromOffsets(start, end).normalize(mode.type)
        when (mode.type) {
            ShapeType.Triangle -> path.drawTriangle(size)
            ShapeType.Square -> path.drawRect(size)
            ShapeType.Circle, ShapeType.Oval -> path.drawOval(size)
            ShapeType.Line -> path.drawLine(start.x, start.y, end.x, end.y)
            null -> {}
        }
        path.translate(Offset(minOf(start.x, end.x), minOf(start.y, end.y)))
        val style = if (mode.fill == true) Fill else Stroke(width =
        mode.strokeWidth ?: 10f)
        if (mode.color != null) drawPath(path, color = mode.color, style = style)
    }
}

private fun Size.normalize(type: ShapeType?) =
    if (type == ShapeType.Circle) Size(maxDimension, maxDimension)
    else this

private fun sizeFromOffsets(
    start: Offset,
    end: Offset
) = Size(abs(start.x - end.x), abs(start.y - end.y))

private fun createUObj(start: Offset, end: Offset, mode: BoardToolMode.Shape): UObject {
    val name = requireNotNull(mode.type?.remoteName)
    return RemoteObject.create(name) {
        this["top"] = JsonPrimitive(minOf(start.y, end.y))
        this["left"] = JsonPrimitive(minOf(start.x, end.x))
        val size = sizeFromOffsets(start, end).normalize(mode.type)
        this["width"] = JsonPrimitive(size.width)
        this["height"] = JsonPrimitive(size.height)

        if (mode.type == ShapeType.Circle || mode.type == ShapeType.Oval) {
            this["rx"] = JsonPrimitive(size.width / 2)
            this["ry"] = JsonPrimitive(size.height / 2)
        }
        val color = requireNotNull(mode.color?.asCSSString())
        if (mode.type != ShapeType.Line) {
            val fill =
                if (mode.fill == true) color else  "transparent"
            this["fill"] = JsonPrimitive(fill)
        } else {
            this["x1"] = JsonPrimitive(start.x)
            this["y1"] = JsonPrimitive(start.y)
            this["x2"] = JsonPrimitive(end.x)
            this["y2"] = JsonPrimitive(end.y)
        }
        this["stroke"] = JsonPrimitive(color)
        this["strokeWidth"] = JsonPrimitive(requireNotNull(mode.strokeWidth))
    }
}