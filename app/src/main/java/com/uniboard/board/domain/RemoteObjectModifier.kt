package com.uniboard.board.domain

import kotlinx.coroutines.flow.Flow

interface RemoteObjectModifier {
    val connection: Connection
    suspend fun send(update: UObjectUpdate): Result<Unit>
    fun receive(): Flow<UObjectUpdate>
}