package com.uniboard.board.domain

import kotlinx.coroutines.flow.Flow

interface RemoteObjectRepository {
    suspend fun allObjects(): Result<List<UObject>>
}