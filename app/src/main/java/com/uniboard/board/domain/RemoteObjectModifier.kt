package com.uniboard.board.domain

import kotlinx.coroutines.flow.Flow

interface RemoteObjectModifier {
    suspend fun send(update: UObjectUpdate): Result<Unit>
    fun receive(): Flow<UObjectUpdate>
}