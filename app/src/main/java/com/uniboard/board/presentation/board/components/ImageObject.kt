package com.uniboard.board.presentation.board.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage
import com.uniboard.board.presentation.board.UiUObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun ImageObject(obj: UiUObject, modifier: Modifier = Modifier) {
    val url = remember(obj) {
        val id = obj.state["uniboardData"]?.jsonObject?.get("data")?.jsonPrimitive?.content
        obj.baseUrl + "/storage/$id"
    }
    AsyncImage(
        url,
        contentDescription = null,
        modifier
    )
}