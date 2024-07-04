package com.uniboard.board.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.uniboard.board.domain.UObject

@DslMarker
annotation class UiUObjectDSL

interface UiUObjectApi {
    val type: String
    val features: Set<UiUObjectFeature>
}

sealed interface UiUObjectFeature {
    data class Content(val content: @Composable (obj: UiUObject, modifier: Modifier) -> Unit) :
        UiUObjectFeature

    data class Creator(val content: @Composable (mode: BoardToolMode, onCreate: (UObject) -> Unit, modifier: Modifier) -> Unit) :
        UiUObjectFeature

    data class ToolbarOptions(val content: @Composable (mode: BoardToolMode, onSelect: (mode: BoardToolMode) -> Unit, modifier: Modifier) -> Unit) :
        UiUObjectFeature

    data class Icon(val content: @Composable (modifier: Modifier) -> Unit) : UiUObjectFeature
}

interface UiUObjectScope {
    @UiUObjectDSL
    fun content(content: @Composable (obj: UiUObject, modifier: Modifier) -> Unit)

    @UiUObjectDSL
    fun creator(content: @Composable (mode: BoardToolMode, onCreate: (UObject) -> Unit, modifier: Modifier) -> Unit)

    @UiUObjectDSL
    fun toolbarOptions(content: @Composable (mode: BoardToolMode, onSelect: (mode: BoardToolMode) -> Unit, modifier: Modifier) -> Unit)

    @UiUObjectDSL
    fun icon(content: @Composable (modifier: Modifier) -> Unit)
}

@UiUObjectDSL
fun UiUObjectApi(type: String, content: UiUObjectScope.() -> Unit): UiUObjectApi {
    val features = mutableSetOf<UiUObjectFeature>()
    val scope = object : UiUObjectScope {
        override fun content(content: @Composable (obj: UiUObject, modifier: Modifier) -> Unit) {
            features.add(UiUObjectFeature.Content(content))
        }

        override fun creator(content: @Composable (mode: BoardToolMode, onCreate: (UObject) -> Unit, modifier: Modifier) -> Unit) {
            features.add(UiUObjectFeature.Creator(content))
        }

        override fun toolbarOptions(content: @Composable (mode: BoardToolMode, onSelect: (mode: BoardToolMode) -> Unit, modifier: Modifier) -> Unit) {
            features.add(UiUObjectFeature.ToolbarOptions(content))
        }

        override fun icon(content: @Composable (modifier: Modifier) -> Unit) {
            features.add(UiUObjectFeature.Icon(content))
        }
    }
    scope.content()
    return object : UiUObjectApi {
        override val type = type
        override val features = features
    }
}