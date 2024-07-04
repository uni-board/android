package com.uniboard.board.presentation.components

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.uniboard.board.domain.RootModule
import com.uniboard.board.domain.UObject

@Composable
fun RootModule.PdfObjectCreator(onCreate: (UObject) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val options = remember {
        FilePickerOptions(
            ActivityResultContracts.StartActivityForResult(),
            fileChooserIntent("application/pdf"),
            getUri = { it.data?.data },
            size = { uri ->
                pdfRenderer.measureSize(context.contentResolver.openInputStream(uri)!!)
            }
        )
    }
    FileObjectCreator(
        type = "uniboard/pdf",
        rootType = "group",
        options = options,
        onCreate = onCreate,
        modifier = modifier
    )
}