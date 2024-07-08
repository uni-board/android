package com.uniboard.onnboarding.domain

interface RecentBoardsRepository {
    suspend fun addBoard(id: String)
    suspend fun removeBoard(id: String)
    suspend fun getBoards(): List<String>
}