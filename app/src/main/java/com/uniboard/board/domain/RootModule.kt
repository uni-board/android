package com.uniboard.board.domain

import com.uniboard.onnboarding.domain.BoardCreatorRepository

interface RootModule {
    fun remoteObjectRepository(id: String): RemoteObjectRepository
    fun remoteObjectModifier(id: String): RemoteObjectModifier
    val baseUrl: String
    val fileRepository: FileRepository
    val pdfRenderer: PdfRenderer
    val boardCreatorRepository: BoardCreatorRepository
}