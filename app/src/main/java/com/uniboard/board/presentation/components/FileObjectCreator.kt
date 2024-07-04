package com.uniboard.board.presentation.components

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import com.uniboard.board.domain.RemoteObject
import com.uniboard.board.domain.RootModule
import com.uniboard.board.domain.UObject
import com.uniboard.core.presentation.rememberState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import java.io.File

data class FilePickerOptions<I, O>(
    val contract: ActivityResultContract<I, O>,
    val intent: I,
    val getUri: (O) -> Uri?,
    val size: (Uri) -> Size,
    val fileName: ((Uri) -> String)? = null
)

@Composable
fun RootModule.FileObjectCreator(
    onCreate: (UObject) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val options = remember {
        FilePickerOptions(
            contract = ActivityResultContracts.StartActivityForResult(),
            intent = fileChooserIntent(),
            getUri = { it.data?.data },
            size = { Size(100f, 100f) },
            fileName = { context.getFileName(it) ?: "Unknown" }
        )
    }
    FileObjectCreator("uniboard/file", "group", options, onCreate, modifier)
}

@Composable
fun <I, O> RootModule.FileObjectCreator(
    type: String,
    rootType: String,
    options: FilePickerOptions<I, O>,
    onCreate: (UObject) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope { Dispatchers.IO }
    var offset by rememberState { Offset.Zero }
    val result =
        rememberLauncherForActivityResult(options.contract) { data ->
            scope.launch {
                val uri = options.getUri(data)
                if (uri != null) {
                    uploadFile(
                        type = type,
                        rootType = rootType,
                        context = context,
                        uri = uri,
                        center = offset,
                        size = options.size(uri),
                        fileName = options.fileName?.invoke(uri),
                        onCreate = onCreate
                    )
                }
            }
        }
    LaunchedEffect(Unit) {
        result.launch(options.intent)
    }
    Box(modifier.onPlaced {
        offset = it.boundsInWindow().center
    })
}


fun fileChooserIntent(mimeType: String = "*/*"): Intent {
    val data = Intent(Intent.ACTION_GET_CONTENT)
    data.addCategory(Intent.CATEGORY_OPENABLE)
    data.setType(mimeType)
    return Intent.createChooser(data, "Choose a file")
}

private suspend fun RootModule.uploadFile(
    type: String,
    rootType: String,
    context: Context,
    uri: Uri,
    center: Offset,
    size: Size,
    fileName: String?,
    onCreate: (UObject) -> Unit
) {
    context.contentResolver.openInputStream(uri)?.use {
        val result = fileRepository.upload(it)
        result.onSuccess { id ->
            val obj = RemoteObject.create(type, rootType) {
                this["top"] = JsonPrimitive(center.y - size.height / 2)
                this["left"] = JsonPrimitive(center.x - size.width / 2)
                this["width"] = JsonPrimitive(size.width)
                this["height"] = JsonPrimitive(size.height)
                val fileMap = if (fileName != null) mapOf("fileName" to JsonPrimitive(fileName)) else emptyMap()
                this["uniboardData"] = JsonObject(
                    this["uniboardData"]!!.jsonObject + mapOf(
                        "data" to JsonPrimitive(id)
                    ) + fileMap
                )
            }
            onCreate(obj)
        }
    }
}

private fun Context.getFileName(uri: Uri): String? = when(uri.scheme) {
    ContentResolver.SCHEME_CONTENT -> getContentFileName(uri)
    else -> uri.path?.let(::File)?.name
}

private fun Context.getContentFileName(uri: Uri): String? = runCatching {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        cursor.moveToFirst()
        return@use cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME).let(cursor::getString)
    }
}.getOrNull()