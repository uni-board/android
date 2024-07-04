package com.uniboard.board.presentation.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.uniboard.board.domain.RemoteObject
import com.uniboard.board.domain.UObject
import com.uniboard.board.presentation.BoardToolMode
import com.uniboard.board.presentation.BoardToolModeState
import com.uniboard.board.presentation.util.asCSSString
import kotlinx.serialization.json.JsonPrimitive


@Composable
fun TextObjectCreator(
    mode: BoardToolModeState,
    onCreate: (UObject) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = remember(mode) {
        mode.state["color"] as? Color
    }
    Box(modifier.pointerInput(Unit) {
        detectTapGestures { offset ->
            onCreate(RemoteObject.create("textbox") {
                this["text"] = JsonPrimitive("text")
                this["top"] = JsonPrimitive(offset.y)
                this["left"] = JsonPrimitive(offset.x)
                this["width"] = JsonPrimitive(100)
                this["height"] = JsonPrimitive(100)
                this["fill"] = JsonPrimitive(requireNotNull(color?.asCSSString()))
            })
        }
    })
}
