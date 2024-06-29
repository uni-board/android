package com.uniboard.board.presentation.board.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.uniboard.board.presentation.board.UiUObject
import com.uniboard.board.presentation.board.dpSize
import com.uniboard.board.presentation.board.size
import com.uniboard.core.presentation.components.AutoSizeText
import com.uniboard.core.presentation.components.AutoSizeTextField
import com.uniboard.core.presentation.rememberState
import kotlinx.serialization.json.JsonArray
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
        when (colorStr) {
            "blue" -> Color.Blue
            "green" -> Color.Green
            "red" -> Color.Red
            "yellow" -> Color.Yellow
            else -> Color.Black
        }
    }
    val textColor = remember(color) {
        if (color == Color.Black) Color.White
        else Color.Black
    }
    Box(modifier.background(color)) {
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