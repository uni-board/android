package com.uniboard.board.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniboard.board.domain.RootModule
import com.uniboard.board.presentation.UiUObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun RootModule.FileObject(obj: UiUObject, modifier: Modifier = Modifier) {
    val fileName = remember(obj) {
        requireNotNull(obj.state["uniboardData"]?.jsonObject?.get("fileName")?.jsonPrimitive?.content)
    }
    val objId = remember(obj) {
        requireNotNull(obj.state["uniboardData"]?.jsonObject?.get("data")?.jsonPrimitive?.content)
    }
    val scope = rememberCoroutineScope { Dispatchers.IO }
    Column(
        modifier
            .wrapContentSize(unbounded = true)
            .background(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.shapes.large)
            .clickable {
                scope.launch {
                    fileRepository.downloadToDevice(objId, fileName)
                }
            }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onTertiaryContainer) {
            Icon(Icons.Default.FileOpen, contentDescription = null)
            Text(fileName, style = MaterialTheme.typography.bodyLarge)
        }
    }
}