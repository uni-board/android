package com.uniboard.board.domain

import java.io.InputStream

interface PdfConverter {
    fun convert(stream: InputStream): Sequence<ByteArray>
}