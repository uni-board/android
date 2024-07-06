package com.uniboard.help.presentation

import com.uniboard.R

object ItemsRepository {
    val items: List<ListItem> = listOf(
        ListItem(1, "Инструмент просмотра", R.drawable.visibility, "Это инструмент просмотра доски, нажав на него можно просмотреть все что есть на доске, не боясь что то испортить, удалить и т.п. .", listOf(R.drawable.screen_visible1, R.drawable.screen_visible2)),
        ListItem(2, "Инструмент удаления", R.drawable.delete_icon, "Это инструмент для удаления объектов с доски, выбрав его можно нажатием удалять элементы на доске", listOf(R.drawable.screen_delete1, R.drawable.screen_delete2))
    )
}