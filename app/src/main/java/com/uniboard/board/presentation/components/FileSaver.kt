package com.uniboard.board.presentation.components

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.uniboard.core.presentation.rememberState
import java.io.InputStream

@Composable
fun rememberFileSaver(): (name: String, mimeType: String, stream: InputStream) -> Unit {
    val context = LocalContext.current
    var saveStream by rememberState<InputStream?> { null }
    val activity = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        if (result.resultCode == Activity.RESULT_OK && uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                saveStream?.copyTo(output)
            }
        }
    }
    return { name, mimeType, stream ->
        saveStream = stream
        activity.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, name)
        })
    }
}