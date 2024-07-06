package com.uniboard.help.presentation

data class ListItem(
    val id: Int,
    val shortText: String,
    val smallImageResId: Int, // Resource ID для маленького изображения
    val detailedText: String,
    val detailedImages: List<Int> // Список ID ресурсов для подробных изображений
)
