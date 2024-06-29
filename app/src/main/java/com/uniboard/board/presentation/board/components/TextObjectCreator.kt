package com.uniboard.board.presentation.board.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.uniboard.board.domain.RemoteObject
import com.uniboard.board.domain.UObject
import com.uniboard.board.presentation.board.BoardToolMode
import com.uniboard.board.presentation.board.util.asCSSString
import kotlinx.serialization.json.JsonPrimitive


@Composable
fun TextObjectCreator(
    mode: BoardToolMode.Text,
    onCreate: (UObject) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.pointerInput(Unit) {
        detectTapGestures { offset ->
            onCreate(RemoteObject.create("textbox") {
                this["text"] = JsonPrimitive("text")
                this["top"] = JsonPrimitive(offset.y)
                this["left"] = JsonPrimitive(offset.x)
                this["width"] = JsonPrimitive(100)
                this["height"] = JsonPrimitive(100)
                this["fill"] = JsonPrimitive(requireNotNull(mode.color?.asCSSString()))
            })
        }
    })
}
