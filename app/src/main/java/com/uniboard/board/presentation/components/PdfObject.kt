package com.uniboard.board.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.uniboard.board.presentation.UiUObject
import com.uniboard.board.presentation.UiUObjectApi
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun PdfObject(obj: UiUObject, modifier: Modifier = Modifier) {
    val pdfId = remember(obj) {
        requireNotNull(obj.state["uniboardData"]?.jsonObject?.get("data")?.jsonPrimitive?.content)
    }
}