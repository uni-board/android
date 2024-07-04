package com.uniboard.board.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.uniboard.board.presentation.BoardToolModeState
import com.uniboard.board.presentation.UiUObjectApi
import com.uniboard.board.presentation.isToolbarSupported
import com.uniboard.board.presentation.toolbar

sealed interface BoardToolbarEvent {
    data class ShowOptions(val mode: BoardToolModeState) : BoardToolbarEvent
    data class SelectMode(val mode: BoardToolModeState) : BoardToolbarEvent
    data object HideOptions : BoardToolbarEvent
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BoardToolbar(
    objectTypes: Set<UiUObjectApi>,
    toolMode: BoardToolModeState,
    onSelect: (event: BoardToolbarEvent) -> Unit,
    modifier: Modifier = Modifier,
    showOptions: Boolean = false
) {
    val scrollState = rememberScrollState()
    SharedTransitionLayout(modifier) {
        Box(
            Modifier
                .shadow(4.dp, MaterialTheme.shapes.extraLarge)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            AnimatedContent(
                showOptions && toolMode.optionsSupported,
                label = "Show Options"
            ) { show ->
                if (show) {
                    val mode = objectTypes.find { it.toolbar?.type == toolMode.type }?.toolbar!!
                    Column(Modifier.fillMaxWidth()) {
                        mode.content?.invoke(
                            toolMode,
                            {
                                onSelect(BoardToolbarEvent.SelectMode(it))
                            },
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        )
                        NavigationRailItem(
                            selected = true,
                            onClick = {
                                onSelect(BoardToolbarEvent.HideOptions)
                            },
                            icon = {
                                mode.icon(modifier)
                            },
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .align(Alignment.End)
                                .sharedElement(
                                    rememberSharedContentState(key = mode.type),
                                    this@AnimatedContent
                                )
                        )
                    }
                } else {
                    Row(
                        Modifier
                            .horizontalScroll(scrollState)
                            .padding(16.dp)
                    ) {
                        objectTypes.filter { it.isToolbarSupported }.forEach { mode ->
                            val modeToolbar = mode.toolbar!!
                            NavigationRailItem(
                                selected = toolMode.type == modeToolbar.type,
                                onClick = { onSelect(BoardToolbarEvent.ShowOptions(modeToolbar.state.value)) },
                                icon = {
                                    modeToolbar.icon(Modifier)
                                },
                                Modifier.sharedElement(
                                    rememberSharedContentState(key = modeToolbar.type),
                                    this@AnimatedContent
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}