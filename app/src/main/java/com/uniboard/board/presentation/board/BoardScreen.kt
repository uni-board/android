package com.uniboard.board.presentation.board

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uniboard.board.domain.RootModule
import com.uniboard.board.presentation.board.components.BoardToolbar
import com.uniboard.board.presentation.board.components.BoardToolbarEvent
import com.uniboard.board.presentation.board.components.UObject
import com.uniboard.board.presentation.board.components.UObjectCreator
import com.uniboard.board.presentation.board.components.transformable
import com.uniboard.core.presentation.theme.UniboardTheme
import kotlinx.serialization.json.JsonObject
import org.http4k.format.KotlinxSerialization.asJsonValue
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RootModule.BoardScreen(id: String, modifier: Modifier = Modifier) {
    val viewModel = viewModel { BoardViewModel(id) }
    val state by viewModel.state.collectAsState()
    BoardScreen(state, modifier)
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BoardScreen(state: BoardScreenState, modifier: Modifier = Modifier) {
    Scaffold(modifier, bottomBar = {
        BoardToolbar(
            state.toolMode, onSelect = { event ->
                when (event) {
                    BoardToolbarEvent.HideOptions -> state.eventSink(BoardScreenEvent.HideToolOptions)
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
            },
            Modifier
                .padding(16.dp)
                .navigationBarsPadding(),
            showOptions = state.showToolOptions
        )
    }) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        val transformState = rememberTransformableState { zoomChange, panChange, rotationChange ->
            if (state.toolMode is BoardToolMode.View) {
                scale *= zoomChange
                offset += panChange
            }
        }
        Box(
            Modifier
                .transformable(transformState)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            state.objects.forEach { obj ->
                val transformedObj by rememberUpdatedState(obj.copy(editable = state.toolMode is BoardToolMode.Edit))
                UObject(transformedObj,
                    onModify = { newObj ->
                        state.eventSink(BoardScreenEvent.TransformObject(transformedObj, newObj))
                    },
                    Modifier
                        .transformable(
                            transformedObj,
                            enabled = state.toolMode is BoardToolMode.Edit
                        ) { scaleX, scaleY, rotation, offset ->
                            state.eventSink(
                                BoardScreenEvent.TransformObject(
                                    obj,
                                    transformedObj.copy(
                                        scaleX = scaleX,
                                        scaleY = scaleY,
                                        angle = rotation,
                                        left = offset.x,
                                        top = offset.y
                                    )
                                )
                            )
                        }
                        .clickable(
                            enabled = state.toolMode is BoardToolMode.Delete,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            state.eventSink(BoardScreenEvent.DeleteObject(transformedObj.id))
                        }
                )
            }
            UObjectCreator(state.toolMode, onCreate = {
                state.eventSink(BoardScreenEvent.CreateObject(it.toUiUObject(state.toolMode is BoardToolMode.Edit)))
            }, Modifier.fillMaxSize())
        }
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
            ),
            toolMode = BoardToolMode.View,
            false
        ) {})
    }
}