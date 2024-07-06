package com.uniboard.board.domain

import kotlinx.serialization.Serializable

@Serializable
data class BoardSettings(
    val name: String? = null,
    val descriptoin: String? = null
)