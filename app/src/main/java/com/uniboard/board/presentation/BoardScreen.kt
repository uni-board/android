package com.uniboard.board.presentation

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uniboard.board.domain.RootModule
import com.uniboard.board.presentation.components.BoardToolbar
import com.uniboard.board.presentation.components.BoardToolbarEvent
import com.uniboard.board.presentation.components.UObject
import com.uniboard.board.presentation.components.UObjectCreator
import com.uniboard.board.presentation.components.transformable
import com.uniboard.core.presentation.theme.UniboardTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.http4k.format.KotlinxSerialization.asJsonValue

@Composable
fun RootModule.BoardScreen(id: String, modifier: Modifier = Modifier) {
    val viewModel = viewModel { BoardViewModel(id) }
    val state by viewModel.state.collectAsState()
    BoardScreen(state, modifier)
}

@Serializable
data class BoardDestination(val id: String)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BoardScreen(state: BoardScreenState, modifier: Modifier = Modifier) {
    val updatedState by rememberUpdatedState(state)
    BackHandler(enabled = updatedState.showMore) {
        if (updatedState.showMore) {
            updatedState.eventSink(BoardScreenEvent.ShowMore(show = false))
        }
    }
    Scaffold(modifier.pointerInput(Unit) {
        detectTapGestures(onPress = {
            if (updatedState.showMore) {
                updatedState.eventSink(BoardScreenEvent.ShowMore(show = false))
            }
        }) {}
    }, bottomBar = {
        BottomBar(state)
    }) {
        Board(state, Modifier.blur(animateDpAsState(if (state.showMore) 8.dp else 0.dp).value))
    }
}

@Composable
private fun Board(state: BoardScreenState, modifier: Modifier = Modifier) {
    BoardCanvas(state, modifier) {
        val updatedState by rememberUpdatedState(state)
        state.objects.forEach { obj ->
            TransformableUObject(obj, updatedState)
        }
        UObjectCreator(updatedState.toolMode, onCreate = {
            updatedState.eventSink(BoardScreenEvent.CreateObject(it.toUiUObject(updatedState.toolMode is BoardToolMode.Edit)))
        }, Modifier.fillMaxSize())
    }
}

@Composable
fun BoardCanvas(
    state: BoardScreenState, modifier: Modifier = Modifier, content: @Composable () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, panChange, rotationChange ->
        if (state.toolMode is BoardToolMode.View) {
            scale *= zoomChange
            offset += panChange
        }
    }
    Box(
        modifier
            .transformable(transformState)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()) {
        content()
    }
}

@Composable
private fun TransformableUObject(
    obj: UiUObject, state: BoardScreenState, modifier: Modifier = Modifier
) {
    val transformedObj by rememberUpdatedState(obj.copy(editable = state.toolMode is BoardToolMode.Edit))
    UObject(transformedObj, onModify = { newObj ->
        state.eventSink(
            BoardScreenEvent.TransformObject(
                transformedObj, newObj
            )
        )
    },
        modifier
            .transformable(
                transformedObj, enabled = state.toolMode is BoardToolMode.Edit
            ) { scaleX, scaleY, rotation, offset ->
                state.eventSink(
                    BoardScreenEvent.TransformObject(
                        transformedObj, transformedObj.copy(
                            scaleX = scaleX,
                            scaleY = scaleY,
                            angle = rotation,
                            left = offset.x,
                            top = offset.y
                        )
                    )
                )
            }
            .border(1.dp, Color.Blue)
            .pointerInput(Unit) {
                detectTapGestures {
                    if (state.toolMode is BoardToolMode.Delete) {
                        state.eventSink(
                            BoardScreenEvent.DeleteObject(
                                transformedObj.id
                            )
                        )
                    }
                }
            })
}

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
private fun BottomBar(
    state: BoardScreenState, modifier: Modifier = Modifier
) {
    SharedTransitionLayout(modifier) {
        AnimatedContent(state.showMore, label = "") { showMore ->
            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
            ) {
                println("SHOWMORE:$showMore")
                if (!showMore) {
                    CollapsedBottomBar(
                        state = state,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@AnimatedContent
                    )
                } else {
                    ExpandedBottomBar(
                        state = state,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@AnimatedContent,
                    )
                }
            }

        }
    }
}

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
private fun ExpandedBottomBar(
    state: BoardScreenState,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    with(sharedTransitionScope) {
        Column(
            Modifier.Companion
                .sharedBounds(
                    sharedTransitionScope.rememberSharedContentState(key = "bounds"),
                    animatedVisibilityScope = animatedContentScope,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
                    clipInOverlayDuringTransition = sharedTransitionScope.OverlayClip(MaterialTheme.shapes.medium)
                )
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .width(200.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NameOption("Board", onEdit = {}, Modifier.fillMaxWidth())
            HelpOption(onClick = {}, Modifier.fillMaxWidth())
            QuitOption(onClick = {}, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun NameOption(title: String, onEdit: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            Modifier
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(8.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        IconButton(onClick = onEdit, colors = IconButtonDefaults.filledIconButtonColors()) {
            Icon(Icons.Default.Edit, contentDescription = null)
        }
    }
}

@Composable
private fun HelpOption(onClick: () -> Unit, modifier: Modifier = Modifier) {
    MenuOption(onClick, modifier, backgroundColor = MaterialTheme.colorScheme.tertiaryContainer) {
        Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null)
        Text("Help")
    }
}

@Composable
private fun QuitOption(onClick: () -> Unit, modifier: Modifier = Modifier) {
    MenuOption(onClick, modifier, backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {
        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
        Text("Exit")
    }
}

@Composable
private fun MenuOption(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColorFor(backgroundColor)) {
            content()
        }
    }
}

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
private fun CollapsedBottomBar(
    state: BoardScreenState,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    with(sharedTransitionScope) {
        FloatingActionButton(
            onClick = {
                state.eventSink(BoardScreenEvent.ShowMore(show = true))
            },
            Modifier
                .padding(bottom = 8.dp)
                .sharedBounds(
                    sharedTransitionScope.rememberSharedContentState(key = "bounds"),
                    animatedVisibilityScope = animatedContentScope,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
                    clipInOverlayDuringTransition = sharedTransitionScope.OverlayClip(MaterialTheme.shapes.medium)
                ), elevation = FloatingActionButtonDefaults.loweredElevation()
        ) {
            Icon(Icons.Default.Menu, contentDescription = null)
        }
        BoardToolbar(
            state.toolMode, onSelect = { event ->
                when (event) {
                    BoardToolbarEvent.HideOptions -> state.eventSink(
                        BoardScreenEvent.HideToolOptions
                    )

                    is BoardToolbarEvent.SelectMode -> state.eventSink(
                        BoardScreenEvent.SetToolMode(
                            event.mode
                        )
                    )

                    is BoardToolbarEvent.ShowOptions -> state.eventSink(
                        BoardScreenEvent.ShowToolOptions(
                            event.mode
                        )
                    )
                }
            }, showOptions = state.showToolOptions
        )
    }
}

@Preview
@Composable
private fun BoardScreenPreview() {
    UniboardTheme {
        BoardScreen(BoardScreenState(
            listOf(
                UiUObject(
                    id = "123",
                    type = "text",
                    top = 100,
                    left = 100,
                    scaleX = 1f,
                    scaleY = 1f,
                    state = JsonObject(
                        mapOf(
                            "text" to "Hello World".asJsonValue(),
                            "left" to (100).asJsonValue(),
                            "top" to (100).asJsonValue(),
                            "width" to (100).asJsonValue()
                        )
                    )
                ),
            ), toolMode = BoardToolMode.View, showToolOptions = false, showMore = false
        ) {})
    }
}