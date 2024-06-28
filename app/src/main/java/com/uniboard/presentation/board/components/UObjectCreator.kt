package com.uniboard.presentation.board.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.uniboard.domain.UObject
import com.uniboard.presentation.board.BoardToolMode

@Composable
fun UObjectCreator(
    mode: BoardToolMode,
    onCreate: (UObject) -> Unit,
    modifier: Modifier = Modifier
) {
    when (mode) {
        is BoardToolMode.Note -> NoteObjectCreator(onCreate, modifier)
        is BoardToolMode.Pen -> PathObjectCreator(mode, onCreate, modifier)
        is BoardToolMode.Shape -> CustomPathObjectCreator(mode, onCreate, modifier)
        BoardToolMode.Text -> TextObjectCreator(onCreate, modifier)
        else -> {}
    }
}
