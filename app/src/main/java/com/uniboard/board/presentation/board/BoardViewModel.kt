package com.uniboard.board.presentation.board

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.uniboard.board.domain.RemoteObject
import com.uniboard.board.domain.RemoteObjectModifier
import com.uniboard.board.domain.RemoteObjectRepository
import com.uniboard.board.domain.RootModule
import com.uniboard.board.domain.UObject
import com.uniboard.board.domain.UObjectUpdate
import com.uniboard.core.presentation.components.toDp
import com.uniboard.util.diffWith
import com.uniboard.util.mutate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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
        val strokeWidth: Float? = null
    ) : BoardToolMode

    data class Text(val color: Color? = null) : BoardToolMode
    data class Note(val color: ColorType? = null) : BoardToolMode
}

enum class ShapeType(val remoteName: String) {
    Triangle("triangle"),
    Square("rect"),
    Circle("ellipse"),
    Oval("ellipse"),
    Line("line")
}

enum class ColorType(val remoteName: String, val color: Color) {
    Red("red", Color.Red),
    Green("green", Color.Green),
    Blue("blue", Color.Blue),
    Yellow("yellow", Color.Yellow),
    Black("black", Color.Black),
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

    data class CreateObject(val obj: UiUObject) : BoardScreenEvent
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
    val editable: Boolean = false,
    val state: Map<String, JsonElement>
)

fun UiUObject.size(): Size? = if (width != null && height != null) Size(width.toFloat(), height.toFloat()) else null
fun UiUObject.dpSize(density: Density): DpSize? = size()?.let { DpSize(density.toDp(it.width), density.toDp(it.height)) }

fun UObject.toUiUObject(editable: Boolean = false) =
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
        editable = editable,
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

    private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)
    val state =
        scope.launchMolecule(RecompositionMode.ContextClock) {
            var toolMode by remember { mutableStateOf<BoardToolMode>(BoardToolMode.View) }
            val objects by produceState(listOf<UiUObject>()) {
                val result = repository.allObjects()
                result.onSuccess { objects ->
                    value = objects.map { it.toUiUObject() }
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
                                        .toUiUObject(toolMode is BoardToolMode.Edit)
                                } else it
                            }
                        }
                    }
                }
            }
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

                    is BoardScreenEvent.CreateObject -> viewModelScope.launch {
                        modifier.send(
                            UObjectUpdate.Add(event.obj.toUObject())
                        )
                        showToolOptions = false
                        delay(500)
                        toolMode = BoardToolMode.Edit
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
