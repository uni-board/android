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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.http4k.format.KotlinxSerialization.asJsonObject


@Composable
fun NoteObjectCreator(
    mode: BoardToolMode.Note,
    onCreate: (UObject) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.pointerInput(Unit) {
        detectTapGestures { offset ->
            onCreate(RemoteObject.create("uniboard/stickyNote") {
                val data = requireNotNull(this["uniboardData"]).jsonObject
                this["uniboardData"] = JsonObject(
                    data + mapOf(
                        "stickerText" to JsonPrimitive("text"),
                        "stickerColor" to JsonPrimitive(requireNotNull( mode.color?.remoteName)),
                    )
                )
                this["top"] = JsonPrimitive(offset.y)
                this["left"] = JsonPrimitive(offset.x)
                this["width"] = JsonPrimitive(420)
                this["height"] = JsonPrimitive(420)
            })
        }
    })
}