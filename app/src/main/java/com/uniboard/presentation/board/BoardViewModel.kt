package com.uniboard.presentation.board

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.uniboard.domain.RemoteObject
import com.uniboard.domain.RemoteObjectModifier
import com.uniboard.domain.RemoteObjectRepository
import com.uniboard.domain.RootModule
import com.uniboard.domain.UObject
import com.uniboard.domain.UObjectUpdate
import com.uniboard.util.diffWith
import com.uniboard.util.mutate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

fun RootModule.BoardViewModel(id: String) =
    BoardViewModel(remoteObjectRepository(id), remoteObjectModifier(id))

@Immutable
data class BoardScreenState(
    val objects: List<UiUObject>,
    val toolMode: BoardToolMode,
    val showToolOptions: Boolean,
    val eventSink: (BoardScreenEvent) -> Unit
)

@Immutable
sealed interface BoardToolMode {
    data object View : BoardToolMode
    data object Edit : BoardToolMode
    data class Pen(val width: Float? = null, val color: Color? = null) : BoardToolMode
    data class Shape(
        val type: ShapeType? = null,
        val fill: Boolean? = null,
        val color: Color? = null,
        val strokeWidth: Int? = null
    ) : BoardToolMode

    data object Text : BoardToolMode
    data class Note(val color: Color? = null) : BoardToolMode
}

enum class ShapeType {
    Triangle,
    Square,
    Circle,
    Oval,
    Line
}

@Immutable
sealed interface BoardScreenEvent {
    data class TransformObject(
        val oldObj: UiUObject,
        val obj: UiUObject
    ) : BoardScreenEvent

    data class SetToolMode(val mode: BoardToolMode) : BoardScreenEvent
    data class ShowToolOptions(val mode: BoardToolMode) : BoardScreenEvent
    data object HideToolOptions : BoardScreenEvent
}

@Immutable
data class UiUObject(
    val id: String,
    val type: String,
    val top: Int = 0,
    val left: Int = 0,
    val width: Int? = null,
    val height: Int? = null,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val angle: Float = 0f,
    val state: Map<String, JsonElement>
)

fun UObject.toUiUObject() =
    UiUObject(
        id = id,
        type = type,
        top = state["top"]?.jsonPrimitive?.content?.toFloat()?.toInt() ?: 0,
        left = state["left"]?.jsonPrimitive?.content?.toFloat()?.toInt() ?: 0,
        width = state["width"]?.jsonPrimitive?.content?.toFloat()?.toInt(),
        height = state["height"]?.jsonPrimitive?.content?.toFloat()?.toInt(),
        scaleX = state["scaleX"]?.jsonPrimitive?.content?.toFloat() ?: 1f,
        scaleY = state["scaleY"]?.jsonPrimitive?.content?.toFloat() ?: 1f,
        angle = state["angle"]?.jsonPrimitive?.content?.toFloat() ?: 0f,
        state = state
    )

fun UiUObject.toUObject() =
    UObject(
        id = id,
        type = type,
        state = state
    )

class BoardViewModel(
    private val repository: RemoteObjectRepository,
    private val modifier: RemoteObjectModifier
) : ViewModel() {

    val state = viewModelScope.launchMolecule(RecompositionMode.ContextClock, AndroidUiDispatcher.Main) {
        val objects by produceState(listOf<UiUObject>()) {
            val result = repository.allObjects()
            result.onSuccess {
                value = it.map(UObject::toUiUObject)
            }
            modifier.receive().collect { update ->
                when (update) {
                    is UObjectUpdate.Add -> value += update.obj.toUiUObject()
                    is UObjectUpdate.Delete -> value = value.filter { it.id != update.id }
                    is UObjectUpdate.Modify -> {
                        val diffId = RemoteObject.idFromDiff(update.diff)
                        value = value.map {
                            if (it.id == diffId) {
                                RemoteObject.toUObjectFromDiff(it.toUObject(), update.diff)
                                    .toUiUObject()
                            } else it
                        }
                    }
                }

            }
        }
        var toolMode by remember { mutableStateOf<BoardToolMode>(BoardToolMode.View) }
        var showToolOptions by remember { mutableStateOf(false) }
        BoardScreenState(
            objects,
            toolMode,
            showToolOptions
        ) { event ->
            when (event) {
                is BoardScreenEvent.TransformObject -> modifyObject(
                    event.oldObj,
                    event.obj
                )

                is BoardScreenEvent.SetToolMode -> toolMode = event.mode
                BoardScreenEvent.HideToolOptions -> showToolOptions = false
                is BoardScreenEvent.ShowToolOptions -> {
                    showToolOptions = true
                    toolMode = event.mode
                }
            }
        }
    }

    private fun modifyObject(
        oldObj: UiUObject,
        obj: UiUObject
    ) = viewModelScope.launch {
        val updatedState = obj.state.mutate {
            this["scaleX"] = JsonPrimitive(obj.scaleX)
            this["scaleY"] = JsonPrimitive(obj.scaleY)
            this["angle"] = JsonPrimitive(obj.angle)
            this["top"] = JsonPrimitive(obj.top.toFloat())
            this["left"] = JsonPrimitive(obj.left.toFloat())
        }
        modifier.send(
            UObjectUpdate.Modify(
                RemoteObject.createDiff(
                    oldObj.toUObject(),
                    obj.toUObject().copy(state = updatedState)
                )
            )
        )
    }
}
