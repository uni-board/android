package com.uniboard.onnboarding.data

import android.content.Context
import androidx.core.content.edit
import com.uniboard.onnboarding.domain.RecentBoardsRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val RECENT_KEY = "recent_boards"

class RecentBoardsRepositoryImpl(context: Context): RecentBoardsRepository {
    private val sharedPreferences = context.getSharedPreferences("recent_boards", Context.MODE_PRIVATE)
    override suspend fun addBoard(id: String) {
        sharedPreferences.edit {
            val recent = getBoards().toMutableList()
            if (id in recent) {
                recent.remove(id)
            }
            recent.add(id)
            putString(RECENT_KEY, Json.encodeToString(recent))
        }
    }

    override suspend fun removeBoard(id: String) {
        sharedPreferences.edit {
            val recent = getBoards().toMutableList()
            if (id in recent) {
                recent.remove(id)
            }
            putString(RECENT_KEY, Json.encodeToString(recent))
        }
    }

    override suspend fun getBoards(): List<String> {
        val recentString = sharedPreferences.getString(RECENT_KEY, "[]") ?: "[]"
        return Json.decodeFromString(recentString)
    }
}