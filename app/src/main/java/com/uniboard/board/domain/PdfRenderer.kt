package com.uniboard.board.domain

import androidx.compose.ui.geometry.Size
import java.io.InputStream

interface PdfRenderer {
    fun convert(stream: InputStream): Sequence<ByteArray>
    fun measureSize(stream: InputStream): Size
}
