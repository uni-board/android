package com.uniboard.board.domain

interface BoardSettingsRepository {
    suspend fun getSettings(): Result<BoardSettings>
    suspend fun setSettings(settings: BoardSettings): Result<Unit>
}