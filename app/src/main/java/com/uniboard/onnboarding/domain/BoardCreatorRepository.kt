package com.uniboard.onnboarding.domain

interface BoardCreatorRepository {
    suspend fun createBoard(): Result<String>
}