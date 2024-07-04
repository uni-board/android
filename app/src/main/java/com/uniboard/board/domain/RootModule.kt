package com.uniboard.board.domain

interface RootModule {
    fun remoteObjectRepository(id: String): RemoteObjectRepository
    fun remoteObjectModifier(id: String): RemoteObjectModifier
    val baseUrl: String
    val fileRepository: FileRepository
    val pdfConverter: PdfConverter
}