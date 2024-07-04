package com.uniboard.board.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.toPath
import com.uniboard.board.presentation.UiUObject
import com.uniboard.board.presentation.util.parseAsRGBAColor
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun PathObject(obj: UiUObject, modifier: Modifier = Modifier) {
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