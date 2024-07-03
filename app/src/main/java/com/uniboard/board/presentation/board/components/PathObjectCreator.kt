package com.uniboard.board.presentation.board.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathSegment
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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
import com.uniboard.core.presentation.theme.UniboardTheme
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import org.http4k.format.KotlinxSerialization.asJsonArray
import org.http4k.format.KotlinxSerialization.asJsonValue

private const val ACTION_IDLE = 0
private const val ACTION_DOWN = 1
private const val ACTION_MOVE = 2
private const val ACTION_UP = 3

@Composable
fun PathObjectCreator(
    mode: BoardToolMode.Pen,
    onCreate: (UObject) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = mode.color ?: Color.Black
    val width = mode.width ?: 5f
    val path = remember { Path() }
    var motionEvent by remember { mutableIntStateOf(ACTION_IDLE) }
    var currentPosition by remember { mutableStateOf(Offset.Unspecified) }

    val drawModifier = modifier
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown().also {
                    motionEvent = ACTION_DOWN
                    currentPosition = it.position
                }
                do {
                    val event: PointerEvent = awaitPointerEvent()
                    event.changes
                        .forEachIndexed { _, pointerInputChange ->
                            if (pointerInputChange.positionChange() != Offset.Zero) pointerInputChange.consume()
                        }
                    motionEvent = ACTION_MOVE
                    currentPosition = event.changes.first().position
                } while (event.changes.any { it.pressed })

                motionEvent = ACTION_UP
            }
        }




    Canvas(modifier = drawModifier) {
        println("CURRENT=$currentPosition")
        when (motionEvent) {
            ACTION_DOWN -> {
                println("MOVE $currentPosition")
                path.moveTo(currentPosition.x, currentPosition.y)
            }

            ACTION_MOVE -> {
                if (currentPosition != Offset.Unspecified) {
                    println("LINE $currentPosition")
                    path.lineTo(currentPosition.x, currentPosition.y)
                }
            }

            ACTION_UP -> {
                println("CREATE $currentPosition")
                path.lineTo(currentPosition.x, currentPosition.y)
                onCreate(path.createUObject(color, width))
                motionEvent = ACTION_IDLE
            }

            else -> Unit
        }

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