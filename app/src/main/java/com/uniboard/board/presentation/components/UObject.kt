package com.uniboard.board.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.uniboard.board.presentation.UiUObject
import com.uniboard.board.presentation.UiUObjectApi
import com.uniboard.board.presentation.UiUObjectFeature
import com.uniboard.board.presentation.content

@Composable
fun UObject(
    objects: Set<UiUObjectApi>,
    obj: UiUObject,
    onModify: (UiUObject) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentObj = remember(obj) {
        objects.find { it.matches(obj.type) }?.content
    }
    currentObj?.content?.invoke(obj, onModify, modifier)
}