package com.uniboard.board.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import com.uniboard.board.presentation.BoardToolModeState
import com.uniboard.board.presentation.UiUObject
import com.uniboard.board.presentation.UiUObjectApi
import com.uniboard.board.presentation.copyWith
import com.uniboard.board.presentation.icon
import com.uniboard.board.presentation.util.parseAsRGBAColor
import com.uniboard.core.presentation.components.AutoSizeTextField
import com.uniboard.core.presentation.rememberState
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

fun TextObject() = UiUObjectApi {
    type { it == "textbox" }
    content { obj, onEdit, modifier ->
        TextObject(obj, onEdit, modifier)
    }
    creator { mode, onCreate, modifier ->
        TextObjectCreator(mode, onCreate, modifier)
    }
    toolbar("text") {
        options { mode, onSelect, modifier ->
            TextOptions(mode, onSelect, modifier)
        }
        icon(Icons.Default.TextFields)
    }
}

@Composable
fun TextObject(obj: UiUObject, onEdit: (UiUObject) -> Unit, modifier: Modifier = Modifier) {
    val color = remember(obj) {
        obj.state["fill"]?.jsonPrimitive?.content?.parseAsRGBAColor() ?: Color.Green
    }
    val text = remember(obj) {
        obj.state["text"]?.jsonPrimitive?.content ?: ""
    }
    var textState by rememberState(text) { text }
    LaunchedEffect(textState) {
        onEdit(obj.copy(state = obj.state + ("text" to JsonPrimitive(textState))))
    }
    AutoSizeTextField(
        value = textState,
        onValueChange = {
            textState = it
        },
        modifier = modifier,
        textStyle = TextStyle(color = color),
        enabled = obj.editable,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        cursorBrush = SolidColor(color)
    )
}

@Composable
fun TextOptions(
    selectedMode: BoardToolModeState,
    onSelect: (BoardToolModeState) -> Unit,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues()
) {
    val color = remember(selectedMode) {
        selectedMode.state["color"] as? Color
    }
    ColorCarousel(color ?: Color.Black, onSelect = {
        onSelect(selectedMode.copyWith { this["color"] = it })
    }, modifier = modifier, padding = padding)
}