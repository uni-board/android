package com.uniboard.board.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import com.uniboard.board.presentation.ColorType
import com.uniboard.board.presentation.UiUObject
import com.uniboard.board.presentation.dpSize
import com.uniboard.core.presentation.components.AutoSizeTextField
import com.uniboard.core.presentation.rememberState
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun NoteObject(obj: UiUObject, onModify: (UiUObject) -> Unit, modifier: Modifier = Modifier) {
    val text = remember(obj) {
        obj.state["uniboardData"]?.jsonObject?.get("stickerText")?.jsonPrimitive?.content ?: ""
    }
    val color = remember(obj) {
        val colorStr =
            obj.state["uniboardData"]?.jsonObject?.get("stickerColor")?.jsonPrimitive?.content ?: ""
        ColorType.entries.find { it.remoteName == colorStr }?.color ?: Color.Black
    }
    val textColor = remember(color) {
        if (color == Color.Black) Color.White
        else Color.Black
    }
    var textState by rememberState(text) { text }
    LaunchedEffect(textState) {
        val data = obj.state["uniboardData"]?.jsonObject ?: JsonObject(mapOf())
        onModify(
            obj.copy(
                state = obj.state + ("uniboardData" to JsonObject(
                    data + mapOf(
                        "stickerText" to JsonPrimitive(
                            textState
                        )
                    )
                ))
            )
        )
    }
    val density = LocalDensity.current
    val size = obj.dpSize(density)
    Box(modifier.background(color)) {
        AutoSizeTextField(
            value = textState,
            onValueChange = {
                textState = it
            },
            modifier = Modifier.then(
                if (size != null) Modifier.size(size) else Modifier
            ),
            textStyle = TextStyle(color = textColor),
            enabled = obj.editable,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            cursorBrush = SolidColor(textColor)
        )
    }
}