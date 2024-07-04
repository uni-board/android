package com.uniboard.board.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.ShapeLine
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uniboard.board.presentation.BoardToolMode
import com.uniboard.board.presentation.ColorType
import com.uniboard.board.presentation.ShapeType
import com.uniboard.core.presentation.theme.UniboardTheme

sealed interface BoardToolbarEvent {
    data class ShowOptions(val mode: BoardToolMode) : BoardToolbarEvent
    data class SelectMode(val mode: BoardToolMode) : BoardToolbarEvent
    data object HideOptions : BoardToolbarEvent
}

private data class ToolMode<out T : BoardToolMode>(
    val icon: ImageVector,
    val name: String,
    val state: T,
    val options: (@Composable (selected: @UnsafeVariance T, onSelect: (T) -> Unit, padding: PaddingValues, modifier: Modifier) -> Unit)? = null
)

private fun BoardToolMode.toToolMode() =
    availableModes.first { this::class.simpleName == it.state::class.simpleName }

private val availableModes = mutableStateListOf(
    ToolMode(Icons.Default.Visibility, "View", BoardToolMode.View),
    ToolMode(Icons.Default.Edit, "Edit", BoardToolMode.Edit),
    ToolMode(Icons.Default.Delete, "Delete", BoardToolMode.Delete),
    ToolMode(
        Icons.Default.Draw,
        "Pen",
        BoardToolMode.Pen()
    ) { selected, onSelect, padding, modifier ->
        PenOptions(selected, onSelect, modifier, padding)
    },
    ToolMode(
        Icons.Default.NoteAlt,
        "Note",
        BoardToolMode.Note()
    ) { selected, onSelect, padding, modifier ->
        NoteOptions(selected, onSelect, modifier, padding)
    },
    ToolMode(
        Icons.Default.ShapeLine,
        "Shape",
        BoardToolMode.Shape()
    ) { selected, onSelect, padding, modifier ->
        ShapeOptions(selected, onSelect, modifier, padding)
    },
    ToolMode(Icons.Default.TextFields, "Text", BoardToolMode.Text()) { selected, onSelect, padding, modifier ->
        TextOptions(selected, onSelect, modifier, padding)
    },
    ToolMode(Icons.Default.Image, "Image", BoardToolMode.Image)
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BoardToolbar(
    selectedMode: BoardToolMode,
    onSelect: (event: BoardToolbarEvent) -> Unit,
    modifier: Modifier = Modifier,
    showOptions: Boolean = false
) {
    val toolMode = selectedMode.toToolMode()
    val scrollState = rememberScrollState()
    SharedTransitionLayout(modifier) {
        Box(
            Modifier
                .shadow(4.dp, MaterialTheme.shapes.extraLarge)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            AnimatedContent(
                showOptions && toolMode.options != null,
                label = "Show Options"
            ) { show ->
                if (show && toolMode.options != null) {
                    Column(Modifier.fillMaxWidth()) {
                        toolMode.options.invoke(
                            selectedMode,
                            {
                                onSelect(BoardToolbarEvent.SelectMode(it))
                            },
                            PaddingValues(horizontal = 16.dp),
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        )
                        NavigationRailItem(
                            selected = true,
                            onClick = {
                                onSelect(BoardToolbarEvent.HideOptions)
                            },
                            icon = {
                                Icon(
                                    toolMode.icon,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .align(Alignment.End)
                                .sharedElement(
                                    rememberSharedContentState(key = toolMode.name),
                                    this@AnimatedContent
                                )
                        )
                    }
                } else {
                    Row(
                        Modifier
                            .horizontalScroll(scrollState)
                            .padding(16.dp)
                    ) {
                        availableModes.forEach { mode ->
                            NavigationRailItem(
                                selected = toolMode.name == mode.name,
                                onClick = { onSelect(BoardToolbarEvent.ShowOptions(mode.state)) },
                                icon = {
                                    Icon(mode.icon, contentDescription = null)
                                },
                                Modifier.sharedElement(
                                    rememberSharedContentState(key = mode.name),
                                    this@AnimatedContent
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun PenOptions(
    selectedMode: BoardToolMode.Pen,
    onSelect: (BoardToolMode.Pen) -> Unit,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues()
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ColorCarousel(
            selectedMode.color ?: Color.Black,
            onSelect = { onSelect(selectedMode.copy(color = it)) }, padding = padding
        )
        Slider(selectedMode.width ?: 5f, onValueChange = {
            onSelect(selectedMode.copy(width = it))
        }, valueRange = 5f..60f, modifier = Modifier.padding(padding))
    }
}

private val defaultColors = listOf(
    Color.Red,
    Color.Yellow,
    Color.Green,
    Color.Blue,
    Color.Cyan,
    Color.Black,
    Color.DarkGray,
    Color.Gray,
    Color.LightGray,
    Color.Magenta,
    Color.White
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorCarousel(
    selectedColor: Color,
    onSelect: (Color) -> Unit,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(),
    colors: List<Color> =  defaultColors
) {
   val state = rememberCarouselState { colors.size }
    HorizontalUncontainedCarousel(
        state,
        modifier = modifier.fillMaxWidth(),
        itemWidth = 48.dp,
        itemSpacing = 4.dp,
        contentPadding = padding
    ) { index ->
        val color = colors[index]
        Box(
            Modifier
                .maskBorder(
                    BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.tertiary
                    ), CircleShape
                )
                .maskClip(CircleShape)
                .size(48.dp)
                .background(color)
                .clickable {
                    onSelect(color)
                    println("Selected")
                }, contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                color == selectedColor,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                Icon(
                    Icons.Default.Check, contentDescription = null,
                    tint = if (color.luminance() > 0.5f) Color.Black else Color.White
                )
            }
        }
    }
}

@Composable
private fun NoteOptions(
    selectedMode: BoardToolMode.Note,
    onSelect: (BoardToolMode.Note) -> Unit,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues()
) {
    ColorCarousel(selectedMode.color?.color?: Color.Black, onSelect = { color ->
        onSelect(BoardToolMode.Note(ColorType.entries.first { it.color == color }))
    }, modifier, padding, colors = remember { ColorType.entries.map { it.color } })
}

private data class Shape(
    val type: ShapeType,
    val supportsFill: Boolean = true,
    val path: Path.(size: Size) -> Unit
)

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

@Composable
private fun ShapeOptions(
    selectedMode: BoardToolMode.Shape,
    onSelect: (BoardToolMode.Shape) -> Unit,
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
                    selected = selectedMode.type == shape.type,
                    onClick = { onSelect(selectedMode.copy(type = shape.type)) },
                    icon = {
                        Box(
                            Modifier
                                .drawBehind {
                                    drawPath(
                                        Path().also { shape.path(it, size) }, primary,
                                        style = if (selectedMode.fill == true && shape.supportsFill) Fill else Stroke(
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
                selectedMode.copy(
                    fill = !(selectedMode.fill ?: true)
                )
            )
        }) {
            Icon(
                if (selectedMode.fill == true) Icons.Filled.Circle else Icons.Outlined.Circle,
                null
            )
            Text("Fill")
        }
        Slider(selectedMode.strokeWidth ?: 10f, onValueChange = {
            onSelect(selectedMode.copy(strokeWidth = it))
        }, valueRange = 5f..60f)
        ColorCarousel(selectedMode.color ?: Color.Black, onSelect = {
            onSelect(selectedMode.copy(color = it))
        }, modifier, padding)
    }
}

@Composable
fun TextOptions(
    selectedMode: BoardToolMode.Text,
    onSelect: (BoardToolMode.Text) -> Unit,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues()
) {
    ColorCarousel(selectedMode.color ?: Color.Black, onSelect = {
        onSelect(selectedMode.copy(color = it))
    }, modifier = modifier, padding = padding)
}

@Preview
@Composable
private fun BoardToolbarPreview() {
    UniboardTheme {
        var currentMode by remember { mutableStateOf<BoardToolMode>(BoardToolMode.View) }
        var showOptions by remember { mutableStateOf(false) }
        BoardToolbar(currentMode, onSelect = { event ->
            when (event) {
                BoardToolbarEvent.HideOptions -> showOptions = false
                is BoardToolbarEvent.SelectMode -> currentMode = event.mode
                is BoardToolbarEvent.ShowOptions -> {
                    currentMode = event.mode
                    showOptions = true
                }
            }
        }, showOptions = showOptions)
    }
}

@Preview
@Composable
private fun PenOptionsPreview() {
    UniboardTheme {
        PenOptions(BoardToolMode.Pen(5f, Color.Black), onSelect = {

        })
    }

}

@Preview
@Composable
private fun NoteOptionsPreview() {
    UniboardTheme {
        NoteOptions(BoardToolMode.Note(ColorType.Black), onSelect = {

        })
    }
}

@Preview
@Composable
private fun ShapeOptionsPreview() {
    UniboardTheme {
        var mode by remember { mutableStateOf(BoardToolMode.Shape(type = ShapeType.Square)) }
        ShapeOptions(mode, onSelect = {
            mode = it
        })
    }
}