package com.uniboard.presentation.board.components

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
import com.uniboard.presentation.board.UiUObject
import com.uniboard.presentation.board.util.parseAsRGBAColor
import com.uniboard.presentation.theme.UniboardTheme
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

private fun Path.drawTriangle(size: Size) {
    moveTo(size.width / 2, 0f)
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
}

private fun Path.drawRect(size: Size) {
    addRect(size.toRect())
}

private fun Path.drawOval(size: Size) {
    addOval(size.toRect())
}

private fun Path.drawLine(obj: UiUObject) {
    var x1 = obj.state["x1"]?.jsonPrimitive?.content?.toFloat() ?: 0f
    var y1 = obj.state["y1"]?.jsonPrimitive?.content?.toFloat() ?: 0f
    var x2 = obj.state["x2"]?.jsonPrimitive?.content?.toFloat() ?: 0f
    var y2 = obj.state["y2"]?.jsonPrimitive?.content?.toFloat() ?: 0f
    if (x2 > x1) {
        x1 *= -1
        x2 *= -1
        y1 *= -1
        y2 *= -1
    }
    if (y1 < y2) {
        moveTo(x1 - x2, 0f)
        lineTo(0f, y2 - y1)
    } else {
        moveTo(0f, 0f)
        lineTo(x1 - x2, y1 - y2)
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