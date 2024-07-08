package com.uniboard.board.domain

import com.uniboard.board_details.domain.BoardSettingsRepository

interface RootModule {
    fun remoteObjectRepository(id: String): RemoteObjectRepository
    fun remoteObjectModifier(id: String): RemoteObjectModifier
    fun boardSettingsRepository(id: String): BoardSettingsRepository
    val baseUrl: String
    val fileRepository: FileRepository
    val pdfRenderer: PdfRenderer

}