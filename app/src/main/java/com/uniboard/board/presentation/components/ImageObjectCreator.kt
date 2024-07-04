package com.uniboard.board.presentation.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import com.uniboard.board.domain.RemoteObject
import com.uniboard.board.domain.RootModule
import com.uniboard.board.domain.UObject
import com.uniboard.core.presentation.rememberState
import com.uniboard.util.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject


@Composable
fun RootModule.ImageObjectCreator(onCreate: (UObject) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope { Dispatchers.IO }
    var offset by rememberState { Offset.Zero }
    val result =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                scope.launch {
                    uploadFile(context, uri, offset, onCreate)
                }
            }
        }
    LaunchedEffect(Unit) {
        result.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    Box(modifier.onPlaced {
        offset = it.boundsInWindow().center
    })
}

private suspend fun RootModule.uploadFile(
    context: Context,
    uri: Uri,
    center: Offset,
    onCreate: (UObject) -> Unit
) {
    val bitmap = context.bitmapFromUri(uri)
    val result = fileRepository.upload(bitmap.toByteArray().inputStream())
    result.onSuccess { id ->
        val obj = RemoteObject.create("uniboard/image", "image") {
            this["top"] = JsonPrimitive(center.y - bitmap.height / 2)
            this["left"] = JsonPrimitive(center.x - bitmap.width / 2)
            this["width"] = JsonPrimitive(bitmap.width)
            this["height"] = JsonPrimitive(bitmap.height)
            this["uniboardData"] = JsonObject(
                this["uniboardData"]!!.jsonObject + mapOf(
                    "data" to JsonPrimitive(id)
                )
            )
        }
        onCreate(obj)
    }
}

private fun Context.bitmapFromUri(uri: Uri): Bitmap {
    val parcelFileDescriptor =
        contentResolver.openFileDescriptor(uri, "r")!!
    val fileDescriptor = parcelFileDescriptor.fileDescriptor
    val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
    parcelFileDescriptor.close()
    return image
}