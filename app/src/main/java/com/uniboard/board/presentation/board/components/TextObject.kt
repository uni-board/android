package com.uniboard.board.presentation.board.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import com.uniboard.board.presentation.board.UiUObject
import com.uniboard.board.presentation.board.util.parseAsRGBAColor
import com.uniboard.core.presentation.components.AutoSizeTextField
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun TextObject(obj: UiUObject, onEdit: (UiUObject) -> Unit, modifier: Modifier = Modifier) {
    val color = remember(obj) {
        obj.state["fill"]?.jsonPrimitive?.content?.parseAsRGBAColor() ?: Color.Green
    }
    val text = remember(obj) {
        obj.state["text"]?.jsonPrimitive?.content ?: ""
    }
    var textState by remember(text) { mutableStateOf(text) }
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
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
    )
}