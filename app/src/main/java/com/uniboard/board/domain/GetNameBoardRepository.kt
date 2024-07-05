package com.uniboard.board.domain

interface GetNameBoardRepository {
    suspend fun getName(id: String) : String
}