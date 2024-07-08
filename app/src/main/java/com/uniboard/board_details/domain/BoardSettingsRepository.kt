package com.uniboard.board_details.domain

import com.uniboard.board_details.domain.BoardSettings

interface BoardSettingsRepository {
    suspend fun get(): Result<BoardSettings>
    suspend fun update(boardSettings: BoardSettings): Result<Unit>
}