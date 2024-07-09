package com.uniboard.board_details.presentation.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
@Serializable
data class BoardSettings(
    @SerialName("name")
    val name: String? = null,
    @SerialName("description")
    val description: String? = null
)