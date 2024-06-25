package com.uniboard.domain

import kotlinx.coroutines.flow.Flow

interface RemoteObjectRepository {
    suspend fun allObjects(): Result<List<UObject>>
}