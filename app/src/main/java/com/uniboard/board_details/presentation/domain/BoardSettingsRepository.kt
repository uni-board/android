package com.uniboard.board_details.presentation.domain

import com.uniboard.board_details.presentation.domain.BoardSettings

interface BoardSettingsRepository {
    suspend fun get(): Result<BoardSettings>
    suspend fun update(boardSettings: BoardSettings): Result<Unit>
}