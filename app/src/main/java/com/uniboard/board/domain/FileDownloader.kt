package com.uniboard.board.domain

import java.io.InputStream

interface FileDownloader {
    fun download(id: String): InputStream
    fun downloadToDevice(id: String, name: String)
}