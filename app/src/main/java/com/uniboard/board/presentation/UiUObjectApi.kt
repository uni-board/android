package com.uniboard.board.presentation

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.uniboard.board.domain.UObject
import kotlin.reflect.KClass

@DslMarker
annotation class UiUObjectDSL

interface UiUObjectApi {
    infix fun matches(type: String): Boolean
    val features: Map<KClass<out UiUObjectFeature>, UiUObjectFeature>
    companion object
}

val UiUObjectApi.isToolbarSupported
    get() = features[UiUObjectFeature.ToolbarOptions::class] != null

val UiUObjectApi.toolbar
    get() = features[UiUObjectFeature.ToolbarOptions::class] as? UiUObjectFeature.ToolbarOptions

val UiUObjectApi.content
    get() = features[UiUObjectFeature.Content::class] as? UiUObjectFeature.Content

val UiUObjectApi.creator
    get() = features[UiUObjectFeature.Creator::class] as? UiUObjectFeature.Creator

sealed interface UiUObjectFeature {
    data class Content(val content: @Composable (obj: UiUObject, onModify: (UiUObject) -> Unit, modifier: Modifier) -> Unit) :
        UiUObjectFeature

    data class Creator(
        val content: @Composable (mode: BoardToolModeState, onCreate: (UObject) -> Unit, modifier: Modifier) -> Unit
    ) : UiUObjectFeature

    data class ToolbarOptions(
        val type: String,
        val icon: @Composable (modifier: Modifier) -> Unit,
        val content: (@Composable (mode: BoardToolModeState, onSelect: (mode: BoardToolModeState) -> Unit, modifier: Modifier) -> Unit)?,
        val state: MutableState<BoardToolModeState> = mutableStateOf(BoardToolModeState(type = type, optionsSupported = content != null, state = emptyMap()))
    ) : UiUObjectFeature
}

interface UiUObjectScope {
    @UiUObjectDSL
    fun type(filter: (String) -> Boolean)

    @UiUObjectDSL
    fun content(content: @Composable (obj: UiUObject, onModify: (UiUObject) -> Unit, modifier: Modifier) -> Unit)

    @UiUObjectDSL
    fun creator(
        content: @Composable (mode: BoardToolModeState, onCreate: (UObject) -> Unit, modifier: Modifier) -> Unit
    )

    @UiUObjectDSL
    fun toolbar(type: String, content: UiUObjectToolbarScope.() -> Unit)
}

interface UiUObjectToolbarScope {
    @UiUObjectDSL
    fun options(
        content: @Composable (mode: BoardToolModeState, onSelect: (mode: BoardToolModeState) -> Unit, modifier: Modifier) -> Unit
    )

    @UiUObjectDSL
    fun icon(content: @Composable (modifier: Modifier) -> Unit)
}

@UiUObjectDSL
fun UiUObjectScope.content(content: @Composable (obj: UiUObject, modifier: Modifier) -> Unit) {
    content { obj, _, modifier -> content(obj, modifier) }
}

@UiUObjectDSL
fun UiUObjectToolbarScope.icon(icon: ImageVector) {
    icon { Icon(icon, contentDescription = null) }
}

@UiUObjectDSL
fun UiUObjectApi(content: UiUObjectScope.() -> Unit): UiUObjectApi {
    val features = mutableMapOf<KClass<out UiUObjectFeature>, UiUObjectFeature>()
    var currentFilter: (String) -> Boolean = { false }
    val scope = object : UiUObjectScope {
        override fun type(filter: (String) -> Boolean) {
            currentFilter = filter
        }

        override fun content(content: @Composable (obj: UiUObject, onModify: (UiUObject) -> Unit, modifier: Modifier) -> Unit) {
            features[UiUObjectFeature.Content::class] = UiUObjectFeature.Content(content)
        }

        override fun creator(
            content: @Composable (mode: BoardToolModeState, onCreate: (UObject) -> Unit, modifier: Modifier) -> Unit
        ) {
            features[UiUObjectFeature.Creator::class] = UiUObjectFeature.Creator(content)
        }

        override fun toolbar(type: String, content: UiUObjectToolbarScope.() -> Unit) {
            object : UiUObjectToolbarScope {
                override fun options(content: @Composable (mode: BoardToolModeState, onSelect: (mode: BoardToolModeState) -> Unit, modifier: Modifier) -> Unit) {
                    val previous =
                        (features[UiUObjectFeature.ToolbarOptions::class] as? UiUObjectFeature.ToolbarOptions)?.icon
                    features[UiUObjectFeature.ToolbarOptions::class] =
                        UiUObjectFeature.ToolbarOptions(
                            type = type,
                            icon = previous ?: {},
                            content
                        )
                }

                override fun icon(content: @Composable (modifier: Modifier) -> Unit) {
                    val previous =
                        (features[UiUObjectFeature.ToolbarOptions::class] as? UiUObjectFeature.ToolbarOptions)?.content
                    features[UiUObjectFeature.ToolbarOptions::class] =
                        UiUObjectFeature.ToolbarOptions(
                            type = type,
                            icon = content,
                            content = previous
                        )
                }

            }.content()
        }
    }
    scope.content()
    return object : UiUObjectApi {
        override fun matches(type: String) = currentFilter(type)
        override val features = features
    }
}