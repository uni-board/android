package com.uniboard.presentation.board.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.uniboard.presentation.board.UiUObject

@Composable
fun UObject(obj: UiUObject, modifier: Modifier = Modifier) {
    when (obj.type) {
        "textbox" -> TextObject(obj, modifier)
        "path" -> PathObject(obj, modifier)
        "triangle", "line", "ellipse", "rect" -> CustomPathObject(obj, modifier)
        "uniboard/stickyNote" -> NoteObject(obj, modifier)
    }
}