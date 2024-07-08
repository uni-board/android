package com.uniboard.board_details.data
import com.uniboard.board.data.to
import com.uniboard.board_details.domain.BoardSettings
import com.uniboard.board_details.domain.BoardSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
class BoardSettingsRepositoryImpl(private val id: String, private val handler: HttpHandler) :
    BoardSettingsRepository {
    override suspend fun get(): Result<BoardSettings> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request(Method.GET, "/board/$id/settings")
            val response = handler(request)
            if (response.bodyString().isBlank()) BoardSettings()
            else handler(request).to<BoardSettings>()
        }
    }

    override suspend fun update(boardSettings: BoardSettings) =
        withContext(Dispatchers.IO) {
            runCatching {
                val request = Request(Method.PUT, "/board/$id/settings/edit")
                    .body(Json.encodeToString(boardSettings).also(::println))
                handler(request)
                Unit
            }
        }
}