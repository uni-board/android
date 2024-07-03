package com.uniboard.board.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uniboard.board.presentation.UiUObject
import com.uniboard.board.presentation.util.parseAsRGBAColor
import com.uniboard.core.presentation.theme.UniboardTheme
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun CustomPathObject(obj: UiUObject, modifier: Modifier = Modifier) {
    val fillColor = remember(obj) {
        val content = obj.state["fill"]?.jsonPrimitive?.content
        if (content == "transparent") null
        else content?.parseAsRGBAColor()
    }
    val strokeColor = remember(obj) {
        obj.state["stroke"]?.jsonPrimitive?.content?.parseAsRGBAColor() ?: Color.Green
    }
    val style = remember(obj) {
        if (fillColor == null) {
            Stroke(
                width = obj.state["strokeWidth"]?.jsonPrimitive?.content?.toFloat() ?: 10f
            )
        } else Fill
    }
    Box(modifier.drawBehind {
        drawPath(Path().apply {
            when (obj.type) {
                "triangle" -> drawTriangle(size)
                "rect" -> drawRect(size)
                "ellipse" -> drawOval(size)
                "line" -> drawLine(obj)
            }
        }, fillColor ?: strokeColor, style = style)
    })
}

fun Path.drawTriangle(size: Size) {
    moveTo(size.width / 2, 0f)
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
}

fun Path.drawRect(size: Size) {
    addRect(size.toRect())
}

fun Path.drawOval(size: Size) {
    addOval(size.toRect())
}

fun Path.drawLine(obj: UiUObject) {
    var x1 = obj.state["x1"]?.jsonPrimitive?.content?.toFloat() ?: 0f
    var y1 = obj.state["y1"]?.jsonPrimitive?.content?.toFloat() ?: 0f
    var x2 = obj.state["x2"]?.jsonPrimitive?.content?.toFloat() ?: 0f
    var y2 = obj.state["y2"]?.jsonPrimitive?.content?.toFloat() ?: 0f
    drawLine(x1, y1, x2, y2)
}

fun Path.drawLine(x1: Float, y1: Float, x2: Float, y2: Float) {
    var nx1 = x1
    var ny1 = y1
    var nx2 = x2
    var ny2 = y2
    if (x2 > x1) {
        nx1 *= -1
        nx2 *= -1
        ny1 *= -1
        ny2 *= -1
    }
    if (ny1 < ny2) {
        moveTo(nx1 - nx2, 0f)
        lineTo(0f, ny2 - ny1)
    } else {
        moveTo(0f, 0f)
        lineTo(nx1 - nx2, ny1 - ny2)
    }
}

@Preview
@Composable
private fun CustomPathObjectPreview() {
    UniboardTheme {
        CustomPathObject(
            UiUObject(
                id = "123",
                type = "customPath",
                top = 100,
                left = 100,
                width = 100,
                height = 100,
                scaleX = 1f,
                scaleY = 1f,
                state = JsonObject(
                    mapOf(
                    )
                )
            ),
            Modifier.size(100.dp)
        )
    }
}