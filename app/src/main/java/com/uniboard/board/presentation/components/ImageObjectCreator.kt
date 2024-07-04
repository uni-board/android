package com.uniboard.board.presentation.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import com.uniboard.board.domain.RootModule
import com.uniboard.board.domain.UObject


@Composable
fun RootModule.ImageObjectCreator(onCreate: (UObject) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val options = remember {
        FilePickerOptions(
            ActivityResultContracts.PickVisualMedia(),
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            { it },
            { context.bitmapFromUri(it).run { Size(width.toFloat(), height.toFloat()) } }
        )
    }
    FileObjectCreator("uniboard/image", "image", options, onCreate, modifier)
}


private fun Context.bitmapFromUri(uri: Uri): Bitmap {
    val parcelFileDescriptor =
        contentResolver.openFileDescriptor(uri, "r")!!
    val fileDescriptor = parcelFileDescriptor.fileDescriptor
    val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
    parcelFileDescriptor.close()
    return image
}