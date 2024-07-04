package com.uniboard.board.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.uniboard.board.domain.RemoteObject
import com.uniboard.board.domain.RemoteObjectModifier
import com.uniboard.board.domain.RemoteObjectRepository
import com.uniboard.board.domain.RootModule
import com.uniboard.board.domain.UObjectUpdate
import com.uniboard.util.mutate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlin.reflect.KClass

fun RootModule.BoardViewModel(id: String) =
    BoardViewModel(
        baseUrl = baseUrl,
        repository = remoteObjectRepository(id),
        modifier = remoteObjectModifier(id)
    )

@Immutable
data class BoardScreenState(
    val objects: List<UiUObject>,
    val toolMode: BoardToolMode,
    val showToolOptions: Boolean,
    val showMore: Boolean,
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
    data object Image: BoardToolMode
    data object File: BoardToolMode
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

    data class ShowMore(val show: Boolean) : BoardScreenEvent
}


class BoardViewModel(
    private val baseUrl: String,
    private val repository: RemoteObjectRepository,
    private val modifier: RemoteObjectModifier
) : ViewModel() {

    private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)
    val state =
        scope.launchMolecule(RecompositionMode.ContextClock) {
            val toolModes = remember {
                mutableStateMapOf(
                    BoardToolMode.View::class to BoardToolMode.View,
                    BoardToolMode.Edit::class to BoardToolMode.Edit,
                    BoardToolMode.Pen::class to BoardToolMode.Pen(
                        width = 10f,
                        color = Color.Black
                    ),
                    BoardToolMode.Shape::class to BoardToolMode.Shape(
                        color = Color.Green,
                        fill = true,
                        type = ShapeType.Triangle,
                        strokeWidth = 10f
                    ),
                    BoardToolMode.Text::class to BoardToolMode.Text(Color.Green),
                    BoardToolMode.Note::class to BoardToolMode.Note(ColorType.Green),
                    BoardToolMode.Delete::class to BoardToolMode.Delete,
                    BoardToolMode.Image::class to BoardToolMode.Image,
                    BoardToolMode.File::class to BoardToolMode.File
                )
            }
            var currentToolMode by remember {
                mutableStateOf<KClass<out BoardToolMode>>(
                    BoardToolMode.View::class
                )
            }
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
            var showMore by remember { mutableStateOf(false) }
            BoardScreenState(
                objects,
                toolModes[currentToolMode] ?: BoardToolMode.View,
                showToolOptions,
                showMore
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
                        toolModes[event.mode::class] =
                            toolModes[event.mode::class]!!.merge(event.mode)
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

                    is BoardScreenEvent.ShowMore -> showMore = event.show
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
