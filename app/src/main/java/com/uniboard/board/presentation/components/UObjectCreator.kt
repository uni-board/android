package com.uniboard.board.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.uniboard.board.domain.RootModule
import com.uniboard.board.domain.UObject
import com.uniboard.board.presentation.BoardToolMode

@Composable
fun RootModule.UObjectCreator(
    mode: BoardToolMode,
    onCreate: (UObject) -> Unit,
    modifier: Modifier = Modifier
) {
    when (mode) {
        is BoardToolMode.Note -> NoteObjectCreator(mode, onCreate, modifier)
        is BoardToolMode.Pen -> PathObjectCreator(mode, onCreate, modifier)
        is BoardToolMode.Shape -> CustomPathObjectCreator(mode, onCreate, modifier)
        is BoardToolMode.Text -> TextObjectCreator(mode, onCreate, modifier)
        is BoardToolMode.Image -> ImageObjectCreator(onCreate, modifier)
        else -> {}
    }
}
