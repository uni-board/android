package com.uniboard.board.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
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
import com.uniboard.board.domain.FileDownloader
import com.uniboard.board.domain.PdfConverter
import com.uniboard.board.domain.RemoteObject
import com.uniboard.board.domain.RemoteObjectModifier
import com.uniboard.board.domain.RemoteObjectRepository
import com.uniboard.board.domain.RootModule
import com.uniboard.board.domain.UObjectUpdate
import com.uniboard.board.presentation.components.ColorType
import com.uniboard.board.presentation.components.CustomPathObject
import com.uniboard.board.presentation.components.FileObject
import com.uniboard.board.presentation.components.ImageObject
import com.uniboard.board.presentation.components.NoteObject
import com.uniboard.board.presentation.components.PathObject
import com.uniboard.board.presentation.components.PdfObject
import com.uniboard.board.presentation.components.TextObject
import com.uniboard.util.mutate
import com.uniboard.util.replace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlin.reflect.KClass

fun RootModule.BoardViewModel(id: String) = BoardViewModel(
    baseUrl = baseUrl,
    repository = remoteObjectRepository(id),
    modifier = remoteObjectModifier(id),
    fileDownloader = fileDownloader,
    pdfConverter = pdfConverter
)

@Immutable
data class BoardScreenState(
    val objects: List<UiUObject>,
    val toolMode: BoardToolModeState,
    val showToolOptions: Boolean,
    val showMore: Boolean,
    val objectTypes: Set<UiUObjectApi>,
    val eventSink: (BoardScreenEvent) -> Unit
)

@Immutable
data class BoardToolModeState(
    val type: String, val optionsSupported: Boolean, val state: Map<String, Any?>
)

val UiUObjectApi.Companion.View
    get() = UiUObjectApi {
        type { it == "view" }
        toolbar("view") { icon(Icons.Default.Visibility) }
    }

val UiUObjectApi.Companion.Edit
    get() = UiUObjectApi {
        type { it == "edit" }
        toolbar("edit") { icon(Icons.Default.Edit) }
    }

val UiUObjectApi.Companion.Delete
    get() = UiUObjectApi {
        type { it == "delete" }
        toolbar("delete") { icon(Icons.Default.Delete) }
    }

fun BoardToolModeState.copyWith(block: MutableMap<String, Any?>.() -> Unit): BoardToolModeState {
    return copy(state = state.toMutableMap().apply(block))
}

fun BoardToolModeState.merge(other: BoardToolModeState): BoardToolModeState {
    if (other.type != type) return other
    return BoardToolModeState(type, other.optionsSupported, state + other.state)
}

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
    Triangle("triangle"), Square("rect"), Circle("ellipse"), Oval("ellipse"), Line("line")
}


@Immutable
sealed interface BoardScreenEvent {
    data class TransformObject(
        val oldObj: UiUObject, val obj: UiUObject
    ) : BoardScreenEvent

    data class SetToolMode(val mode: BoardToolModeState) : BoardScreenEvent
    data class ShowToolOptions(val mode: BoardToolModeState) : BoardScreenEvent
    data object HideToolOptions : BoardScreenEvent

    data class CreateObject(val obj: UiUObject) : BoardScreenEvent
    data class DeleteObject(val id: String) : BoardScreenEvent

    data class ShowMore(val show: Boolean) : BoardScreenEvent
}


class BoardViewModel(
    private val baseUrl: String,
    private val repository: RemoteObjectRepository,
    private val modifier: RemoteObjectModifier,
    private val fileDownloader: FileDownloader,
    private val pdfConverter: PdfConverter
) : ViewModel() {

    private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)
    val state = scope.launchMolecule(RecompositionMode.ContextClock) {
        val objectTypes = remember {
            setOf(
                UiUObjectApi.View,
                UiUObjectApi.Edit,
                UiUObjectApi.Delete,
                PathObject(),
                NoteObject(),
                CustomPathObject(),
                FileObject(),
                ImageObject(),
                TextObject(),
                PdfObject()
            )
        }
        var currentToolMode by remember {
            mutableStateOf(
                UiUObjectApi.View
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
            objects = objects,
            toolMode = objectTypes.find { it.toolbar?.type == currentToolMode.toolbar?.type }?.toolbar?.state?.value
                ?: UiUObjectApi.View.toolbar!!.state.value,
            showToolOptions = showToolOptions,
            showMore = showMore,
            objectTypes = objectTypes
        ) { event ->
            when (event) {
                is BoardScreenEvent.TransformObject -> modifyObject(
                    event.oldObj, event.obj
                )

                is BoardScreenEvent.SetToolMode -> objectTypes.first { it.toolbar?.type == event.mode.type }.toolbar?.state?.value = event.mode
                BoardScreenEvent.HideToolOptions -> showToolOptions = false
                is BoardScreenEvent.ShowToolOptions -> {
                    showToolOptions = true
                    currentToolMode = objectTypes.find { it.toolbar?.state?.value?.type == event.mode.type } ?: UiUObjectApi.View
                    val toolbar = objectTypes.find { it.toolbar?.type == event.mode.type }?.toolbar
                    toolbar?.state?.value = toolbar!!.state.value.merge(event.mode)
                }

                is BoardScreenEvent.CreateObject -> viewModelScope.launch {
                    modifier.send(
                        UObjectUpdate.Add(event.obj.toUObject())
                    )
                    showToolOptions = false
                    delay(500)
                    currentToolMode = UiUObjectApi.Edit
                }

                is BoardScreenEvent.DeleteObject -> viewModelScope.launch {
                    modifier.send(UObjectUpdate.Delete(event.id))
                }

                is BoardScreenEvent.ShowMore -> showMore = event.show
            }
        }
    }

    private fun modifyObject(
        oldObj: UiUObject, obj: UiUObject
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
                    oldObj.toUObject(), obj.toUObject().copy(state = updatedState)
                )
            )
        )
    }
}
