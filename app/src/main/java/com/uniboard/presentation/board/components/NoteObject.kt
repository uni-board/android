package com.uniboard.presentation.board.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.uniboard.presentation.board.UiUObject
import com.uniboard.presentation.core.components.AutoSizeText
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun NoteObject(obj: UiUObject, modifier: Modifier = Modifier) {
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
        AutoSizeText(text, color = textColor)
    }
}