package com.uniboard.board.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniboard.board.presentation.UiUObject
import com.uniboard.board.presentation.UiUObjectApi
import com.uniboard.board.presentation.content
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun FileObject() = UiUObjectApi {
    type { it == "uniboard/file" }
    content { obj, modifier ->
        FileObject(obj, modifier = modifier)
    }
}

@Composable
fun FileObject(obj: UiUObject, modifier: Modifier = Modifier) {
    val fileName = remember(obj) {
        obj.state["uniboardData"]?.jsonObject?.get("fileName")?.jsonPrimitive?.content
    }
    Column(
        modifier
            .wrapContentSize(unbounded = true)
            .background(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.shapes.large)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onTertiaryContainer) {
            Icon(Icons.Default.FileOpen, contentDescription = null)
            if (fileName != null) {
                Text(fileName, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}