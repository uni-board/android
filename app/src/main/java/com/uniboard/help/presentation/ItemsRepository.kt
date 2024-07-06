package com.uniboard.help.presentation

import com.uniboard.R

object ItemsRepository {
    val items: List<ListItem> = listOf(
        ListItem(1, "Tip 1", R.drawable.visibility, "Detailed text for tip 1", listOf(R.drawable.screen_visible1, R.drawable.screen_visible2)),
        ListItem(2, "Tip 2", R.drawable.delete_icon, "Detailed text for tip 2", listOf(R.drawable.screen_delete1, R.drawable.screen_delete2))
    )
}