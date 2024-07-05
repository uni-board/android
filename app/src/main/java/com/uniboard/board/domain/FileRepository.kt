package com.uniboard.board.domain

import java.io.InputStream

interface FileRepository {
    fun download(id: String): InputStream
    suspend fun upload(stream: InputStream): Result<String>
    fun downloadToDevice(id: String, name: String)
}