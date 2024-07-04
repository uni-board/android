package com.uniboard.board.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ShapeLine
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uniboard.board.presentation.BoardToolModeState
import com.uniboard.board.presentation.ShapeType
import com.uniboard.board.presentation.UiUObject
import com.uniboard.board.presentation.UiUObjectApi
import com.uniboard.board.presentation.content
import com.uniboard.board.presentation.copyWith
import com.uniboard.board.presentation.icon
import com.uniboard.board.presentation.util.parseAsRGBAColor
import com.uniboard.core.presentation.theme.UniboardTheme
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

fun CustomPathObject() = UiUObjectApi {
    type { it in setOf("triangle", "rect", "ellipse", "line", "shape") }
    content { obj, modifier ->
        CustomPathObject(obj, modifier)
    }
    creator { mode, onCreate, modifier ->
        CustomPathObjectCreator(mode, onCreate, modifier)
    }
    toolbar("shape") {
        options { mode, onSelect, modifier ->
            ShapeOptions(mode, onSelect, modifier)
        }
        icon(Icons.Default.ShapeLine)
    }
}

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

fun Path.drawLine(obj: UiUObject) {
    val x1 = obj.state["x1"]?.jsonPrimitive?.content?.toFloat() ?: 0f
    val y1 = obj.state["y1"]?.jsonPrimitive?.content?.toFloat() ?: 0f
    val x2 = obj.state["x2"]?.jsonPrimitive?.content?.toFloat() ?: 0f
    val y2 = obj.state["y2"]?.jsonPrimitive?.content?.toFloat() ?: 0f
    drawLine(x1, y1, x2, y2)
}

fun Path.drawRect(size: Size) {
    addRect(size.toRect())
}

fun Path.drawOval(size: Size) {
    addOval(size.toRect())
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

@Composable
fun ShapeOptions(
    selectedMode: BoardToolModeState,
    onSelect: (BoardToolModeState) -> Unit,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues()
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(padding)
        ) {
            val primary = MaterialTheme.colorScheme.primary
            shapes.forEach { shape ->
                NavigationRailItem(
                    selected = selectedMode.state["type"] == shape.type,
                    onClick = {
                        onSelect(selectedMode.copyWith { this["type"] = shape.type })
                    },
                    icon = {
                        Box(
                            Modifier
                                .drawBehind {
                                    drawPath(
                                        Path().also { shape.path(it, size) }, primary,
                                        style = if (selectedMode.state["fill"] == true && shape.supportsFill) Fill else Stroke(
                                            width = 10f
                                        )
                                    )
                                }
                                .size(24.dp)
                        )
                    }
                )
            }
        }
        TextButton(onClick = {
            onSelect(
                selectedMode.copyWith {
                    this["fill"] = !(selectedMode.state["fill"] as? Boolean ?: true)
                }
            )
        }) {
            Icon(
                if (selectedMode.state["fill"] == true) Icons.Filled.Circle else Icons.Outlined.Circle,
                null
            )
            Text("Fill")
        }
        Slider(selectedMode.state["strokeWidth"] as? Float ?: 10f, onValueChange = {
            onSelect(selectedMode.copyWith { this["strokeWidth"] = it })
        }, valueRange = 5f..60f)
        ColorCarousel(selectedMode.state["color"] as? Color ?: Color.Black, onSelect = {
            onSelect(selectedMode.copyWith { this["color"] = it })
        }, modifier, padding)
    }
}

private val shapes = listOf(
    Shape(ShapeType.Triangle) { size -> drawTriangle(size) },
    Shape(ShapeType.Square) { size -> drawRect(size) },
    Shape(ShapeType.Circle) { size -> drawOval(size) },
    Shape(ShapeType.Oval) { size ->
        addOval(Rect(0f, size.height / 6, size.width, size.height * 5 / 6))
    },
    Shape(ShapeType.Line, supportsFill = false) { size ->
        drawLine(size.width, 0f, 0f, size.height)
    },
)

private data class Shape(
    val type: ShapeType,
    val supportsFill: Boolean = true,
    val path: Path.(size: Size) -> Unit
)

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