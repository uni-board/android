package com.uniboard.board.presentation.board

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.uniboard.board.domain.FileDownloader
import com.uniboard.board.domain.RemoteObject
import com.uniboard.board.domain.RemoteObjectModifier
import com.uniboard.board.domain.RemoteObjectRepository
import com.uniboard.board.domain.RootModule
import com.uniboard.board.domain.UObject
import com.uniboard.board.domain.UObjectUpdate
import com.uniboard.core.presentation.components.toDp
import com.uniboard.util.mutate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlin.reflect.KClass

fun RootModule.BoardViewModel(id: String) =
    BoardViewModel(baseUrl, remoteObjectRepository(id), remoteObjectModifier(id), fileDownloader)

@Immutable
data class BoardScreenState(
    val objects: List<UiUObject>,
    val toolMode: BoardToolMode,
    val showToolOptions: Boolean,
    val eventSink: (BoardScreenEvent) -> Unit
)

@Immutable
sealed interface BoardToolMode {
    fun merge(other: BoardToolMode): BoardToolMode = other

    data object View : BoardToolMode
    data object Edit : BoardToolMode
    data class Pen(val width: Float? = null, val color: Color? = null) : BoardToolMode {
        override fun merge(other: BoardToolMode): BoardToolMode {
            if (other is Pen) {
                return Pen(other.width ?: width, other.color ?: color)
            }
            return other
        }
    }

    data class Shape(
        val type: ShapeType? = null,
        val fill: Boolean? = null,
        val color: Color? = null,
        val strokeWidth: Float? = null
    ) : BoardToolMode {
        override fun merge(other: BoardToolMode): BoardToolMode {
            if (other is Shape) {
                return Shape(
                    other.type ?: type,
                    other.fill ?: fill,
                    other.color ?: color,
                    other.strokeWidth ?: strokeWidth
                )
            }
            return other
        }
    }

    data class Text(val color: Color? = null) : BoardToolMode {
        override fun merge(other: BoardToolMode): BoardToolMode {
            if (other is Text) {
                return Text(other.color ?: color)
            }
            return other
        }
    }

    data class Note(val color: ColorType? = null) : BoardToolMode {
        override fun merge(other: BoardToolMode): BoardToolMode {
            if (other is Note) {
                return Note(other.color ?: color)
            }
            return other
        }
    }

    data object Delete : BoardToolMode
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
    data class DeleteObject(val id: String) : BoardScreenEvent
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
    val baseUrl: String = "",
    val state: Map<String, JsonElement>
)

fun UiUObject.size(): Size? =
    if (width != null && height != null) Size(width.toFloat(), height.toFloat()) else null

fun UiUObject.dpSize(density: Density): DpSize? =
    size()?.let { DpSize(density.toDp(it.width), density.toDp(it.height)) }

fun UObject.toUiUObject(editable: Boolean = false, baseUrl: String = "") =
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
        baseUrl = baseUrl,
        state = state
    )

fun UiUObject.toUObject() =
    UObject(
        id = id,
        type = type,
        state = state
    )

class BoardViewModel(
    private val baseUrl: String,
    private val repository: RemoteObjectRepository,
    private val modifier: RemoteObjectModifier,
    private val fileDownloader: FileDownloader
) : ViewModel() {

    private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)
    val state =
        scope.launchMolecule(RecompositionMode.ContextClock) {
            val toolModes = remember {
                mutableStateMapOf(
                    BoardToolMode.View::class to BoardToolMode.View,
                    BoardToolMode.Edit::class to BoardToolMode.Edit,
                    BoardToolMode.Pen:: class to BoardToolMode.Pen(width = 10f, color = Color.Black),
                    BoardToolMode.Shape::class to BoardToolMode.Shape(
                        color = Color.Green,
                        fill = true,
                        type = ShapeType.Triangle,
                        strokeWidth = 10f
                    ),
                    BoardToolMode.Text::class to BoardToolMode.Text(Color.Green),
                    BoardToolMode.Note::class to BoardToolMode.Note(ColorType.Green),
                    BoardToolMode.Delete::class to BoardToolMode.Delete
                )
            }
            var currentToolMode by remember { mutableStateOf<KClass<out BoardToolMode>>(BoardToolMode.View::class) }
            val objects by produceState(listOf<UiUObject>()) {
                val result = repository.allObjects()
                result.onSuccess { objects ->
                    value = objects.map { it.toUiUObject(baseUrl = baseUrl) }
                }
                modifier.receive().collect { update ->
                    when (update) {
                        is UObjectUpdate.Add -> value += update.obj.toUiUObject(baseUrl = baseUrl)
                        is UObjectUpdate.Delete -> value = value.filter { it.id != update.id }
                        is UObjectUpdate.Modify -> {
                            val diffId = RemoteObject.idFromDiff(update.diff)
                            value = value.map {
                                if (it.id == diffId) {
                                    RemoteObject.toUObjectFromDiff(it.toUObject(), update.diff)
                                        .toUiUObject(baseUrl = baseUrl)
                                } else it
                            }
                        }
                    }
                }
            }
            var showToolOptions by remember { mutableStateOf(false) }
            BoardScreenState(
                objects,
                toolModes[currentToolMode] ?: BoardToolMode.View,
                showToolOptions
            ) { event ->
                when (event) {
                    is BoardScreenEvent.TransformObject -> modifyObject(
                        event.oldObj,
                        event.obj
                    )

                    is BoardScreenEvent.SetToolMode -> toolModes[currentToolMode] = event.mode
                    BoardScreenEvent.HideToolOptions -> showToolOptions = false
                    is BoardScreenEvent.ShowToolOptions -> {
                        showToolOptions = true
                        currentToolMode = event.mode::class
                        toolModes[event.mode::class] = toolModes[event.mode::class]!!.merge(event.mode)
                    }

                    is BoardScreenEvent.CreateObject -> viewModelScope.launch {
                        modifier.send(
                            UObjectUpdate.Add(event.obj.toUObject())
                        )
                        showToolOptions = false
                        delay(500)
                        currentToolMode = BoardToolMode.Edit::class
                    }

                    is BoardScreenEvent.DeleteObject -> viewModelScope.launch {
                        modifier.send(UObjectUpdate.Delete(event.id))
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
