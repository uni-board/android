package com.uniboard.board.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.uniboard.board.domain.UObject
import com.uniboard.board.presentation.BoardToolModeState
import com.uniboard.board.presentation.UiUObjectApi
import com.uniboard.board.presentation.creator

@Composable
fun UObjectCreator(
    objects: Set<UiUObjectApi>,
    mode: BoardToolModeState,
    onCreate: (UObject) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentType = remember(mode) {
        objects.find { it.matches(mode.type) }?.creator
    }
    currentType?.content?.invoke(mode, onCreate, modifier)
}
