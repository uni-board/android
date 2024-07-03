package com.uniboard.board.presentation.board.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathSegment
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.copy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uniboard.board.domain.RemoteObject
import com.uniboard.board.domain.UObject
import com.uniboard.board.presentation.board.BoardToolMode
import com.uniboard.board.presentation.board.util.asCSSString
import com.uniboard.core.presentation.rememberState
import com.uniboard.core.presentation.theme.UniboardTheme
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import org.http4k.format.KotlinxSerialization.asJsonArray
import org.http4k.format.KotlinxSerialization.asJsonValue

@Composable
fun PathObjectCreator(
    mode: BoardToolMode.Pen,
    onCreate: (UObject) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = mode.color ?: Color.Black
    val width = mode.width ?: 5f
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

@Preview
@Composable
private fun PathObjectCreatorPreview() {
    UniboardTheme {
        PathObjectCreator(BoardToolMode.Pen(color = Color.Green), onCreate = {
            println(it)
        }, Modifier.size(200.dp))
    }
}