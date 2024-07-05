package com.uniboard.board.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.uniboard.board.domain.RootModule
import com.uniboard.board.presentation.UiUObject

@Composable
fun RootModule.UObject(obj: UiUObject, onModify: (UiUObject) -> Unit, modifier: Modifier = Modifier) {
    when (obj.type) {
        "textbox" -> TextObject(obj, onModify, modifier)
        "path" -> PathObject(obj, modifier)
        "triangle", "line", "ellipse", "rect" -> CustomPathObject(obj, modifier)
        "uniboard/stickyNote" -> NoteObject(obj, onModify, modifier)
        "uniboard/image" -> ImageObject(obj, modifier)
        "uniboard/file" -> FileObject(obj, modifier)
        "uniboard/pdf" -> PdfObject(obj, modifier)
    }
}