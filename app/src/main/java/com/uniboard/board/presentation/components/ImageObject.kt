package com.uniboard.board.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage
import com.uniboard.board.presentation.UiUObject
import com.uniboard.board.presentation.UiUObjectApi
import com.uniboard.board.presentation.content
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun ImageObject() = UiUObjectApi {
    type { it == "uniboard/image" }
    content { obj, modifier ->
        ImageObject(obj, modifier)
    }
}
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