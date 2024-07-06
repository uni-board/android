package com.uniboard.board.presentation

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uniboard.BuildConfig
import com.uniboard.board.domain.RootModule
import com.uniboard.board.presentation.components.BoardToolbar
import com.uniboard.board.presentation.components.BoardToolbarEvent
import com.uniboard.board.presentation.components.UObject
import com.uniboard.board.presentation.components.UObjectCreator
import com.uniboard.board.presentation.components.rememberFileSaver
import com.uniboard.board.presentation.components.transformable
import com.uniboard.board_details.presentation.BoardDetailsDestination
import com.uniboard.core.presentation.ContainerTransformScope
import com.uniboard.core.presentation.DefaultBoundsTransform
import com.uniboard.core.presentation.rememberState
import com.uniboard.core.presentation.sharedBounds
import com.uniboard.help.presentation.HelpDestination
import com.uniboard.onnboarding.presentation.OnboardingDestination
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@Immutable
sealed interface BoardNavigationEvent {
    data object GoToDetails : BoardNavigationEvent
    data object Quit : BoardNavigationEvent
    data object GoToHelp : BoardNavigationEvent
}

@Composable
fun RootModule.BoardScreen(
    id: String,
    transitionScope: ContainerTransformScope,
    onNavigate: (event: BoardNavigationEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = viewModel { BoardViewModel(id) }
    val state by viewModel.state.collectAsState()
    BoardScreen(state, transitionScope, onNavigate, modifier)
}

@Serializable
data class BoardDestination(val id: String)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RootModule.BoardScreen(
    state: BoardScreenState,
    transitionScope: ContainerTransformScope,
    onNavigate: (event: BoardNavigationEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val updatedState by rememberUpdatedState(state)
    val scope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    BackHandler(enabled = updatedState.showMore) {
        if (updatedState.showMore) {
            updatedState.eventSink(BoardScreenEvent.ShowMore(show = false))
        }
    }
    val saver = rememberFileSaver()
    Scaffold(modifier, bottomBar = {
        BottomBar(
            state = state,
            transitionScope = transitionScope,
            onNavigate = onNavigate,
            onSaveBoardClick = {
                scope.launch {
                    val bytes = ByteArrayOutputStream()
                    val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)
                    saver("Board.png", "image/png", ByteArrayInputStream(bytes.toByteArray()))
                }
            }
        )
    }) {
        Board(state, Modifier.drawWithContent {
            graphicsLayer.record {
                this@drawWithContent.drawContent()
            }
            drawLayer(graphicsLayer)
        })
    }
}

@Composable
private fun RootModule.Board(state: BoardScreenState, modifier: Modifier = Modifier) {
    BoardCanvas(state, modifier) {
        val updatedState by rememberUpdatedState(state)
        state.objects.forEach { obj ->
            key(obj.id) {
                TransformableUObject(obj, updatedState)
            }
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
        state.eventSink(BoardScreenEvent.ShowMore(show = false))
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
private fun RootModule.TransformableUObject(
    obj: UiUObject, state: BoardScreenState, modifier: Modifier = Modifier
) {
    val updatedState by rememberUpdatedState(state)
    val transformedObj by rememberUpdatedState(
        obj.copy(
            editable = state.toolMode is BoardToolMode.Edit,
            selectable = state.toolMode is BoardToolMode.View
        )
    )
    val borderModifier = if (BuildConfig.SHOW_BORDERS) Modifier.border(1.dp, Color.Blue) else Modifier
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
            .then(borderModifier)
            .pointerInput(Unit) {
                detectTapGestures {
                    if (updatedState.toolMode is BoardToolMode.Delete) {
                        updatedState.eventSink(
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
    state: BoardScreenState,
    transitionScope: ContainerTransformScope,
    onNavigate: (event: BoardNavigationEvent) -> Unit,
    onSaveBoardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SharedTransitionLayout(modifier) {
        AnimatedContent(state.showMore, label = "") { showMore ->
            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
            ) {
                if (!showMore) {
                    CollapsedBottomBar(
                        state = state,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@AnimatedContent
                    )
                } else {
                    ExpandedBottomBar(
                        onNavigate = onNavigate,
                        onSaveBoardClick = onSaveBoardClick,
                        syncState = state.syncState,
                        onConnect = { state.eventSink(BoardScreenEvent.TrySync) },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        rootTransitionScope = transitionScope,
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
    onNavigate: (event: BoardNavigationEvent) -> Unit,
    onSaveBoardClick: () -> Unit,
    syncState: SyncState,
    onConnect: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    rootTransitionScope: ContainerTransformScope,
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
                .width(300.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NameOption(
                title = "Board",
                onEdit = {
                    onNavigate(BoardNavigationEvent.GoToDetails)
                },
                scope = rootTransitionScope,
                syncState = syncState,
                onConnect = onConnect,
                modifier = Modifier
                    .fillMaxWidth()
            )
            SaveBoardOption(onSaveBoardClick, Modifier.fillMaxWidth())
            HelpOption(
                onClick = {
                    onNavigate(BoardNavigationEvent.GoToHelp)
                },
                Modifier
                    .sharedBounds(
                        rootTransitionScope,
                        HelpDestination
                    )
                    .fillMaxWidth()
            )
            QuitOption(
                onClick = {
                    onNavigate(BoardNavigationEvent.Quit)
                },
                Modifier
                    .sharedBounds(rootTransitionScope, OnboardingDestination)
                    .fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun NameOption(
    title: String,
    syncState: SyncState,
    onConnect: () -> Unit,
    onEdit: () -> Unit,
    scope: ContainerTransformScope,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
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
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = onEdit, Modifier.sharedBounds(
                scope, BoardDetailsDestination, boundsTransform = DefaultBoundsTransform,
                shape = CircleShape,
                resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(ContentScale.Fit)
            ), colors = IconButtonDefaults.filledIconButtonColors()
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
        }
        SyncButton(syncState, onConnect)
    }
}

@Composable
private fun SyncButton(
    syncState: SyncState,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        if (syncState != SyncState.NotSynced) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.error,
        label = "ContainerColor"
    )
    val contentColor by animateColorAsState(
        if (syncState != SyncState.NotSynced) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onError,
        label = "ContentColor"
    )
    val rotation by rememberInfiniteTransition(label = "Rotation")
        .animateFloat(
            0f, 360f, infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "Rotation"
        )
    IconButton(
        onClick = onConnect,
        modifier.graphicsLayer {
            rotationZ = if (syncState == SyncState.SyncInProgress) rotation else 0f
        },
        colors = IconButtonDefaults.outlinedIconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Icon(Icons.Default.Sync, contentDescription = null)
    }
}


@Composable
private fun SaveBoardOption(onClick: () -> Unit, modifier: Modifier = Modifier) {
    MenuOption(onClick, modifier, backgroundColor = MaterialTheme.colorScheme.primaryContainer) {
        Icon(Icons.Default.Screenshot, contentDescription = null)
        Text("Screenshot")
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