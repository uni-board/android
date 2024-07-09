package com.uniboard.board.domain

import com.uniboard.onnboarding.domain.BoardCreatorRepository
import com.uniboard.onnboarding.domain.RecentBoardsRepository

import com.uniboard.board_details.presentation.domain.BoardSettingsRepository

interface RootModule {
    fun remoteObjectRepository(id: String): RemoteObjectRepository
    fun remoteObjectModifier(id: String): RemoteObjectModifier
    fun boardSettingsRepository(id: String): BoardSettingsRepository
    val baseUrl: String
    val fileRepository: FileRepository
    val pdfRenderer: PdfRenderer
    val boardCreatorRepository: BoardCreatorRepository
    val recentBoardsRepository: RecentBoardsRepository

}