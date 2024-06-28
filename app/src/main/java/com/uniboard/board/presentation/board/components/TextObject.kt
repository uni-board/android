package com.uniboard.board.presentation.board.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.uniboard.board.presentation.board.UiUObject
import com.uniboard.board.presentation.board.util.parseAsRGBAColor
import com.uniboard.core.presentation.components.AutoSizeText
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun TextObject(obj: UiUObject, modifier: Modifier = Modifier) {
    val color = remember(obj) {
        obj.state["fill"]?.jsonPrimitive?.content?.parseAsRGBAColor() ?: Color.Green
    }
    val text = remember(obj) {
        obj.state["text"]?.jsonPrimitive?.content ?: ""
    }
    AutoSizeText(text, modifier, color = color)
}