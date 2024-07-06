package com.uniboard.board.data

import com.uniboard.board.domain.BoardSettings
import com.uniboard.board.domain.BoardSettingsRepository
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Request
import org.http4k.format.KotlinxSerialization.json

class BoardSettingsRepositoryImpl : BoardSettingsRepository {
    override suspend fun getSettings(): Result<BoardSettings> {
        TODO("Not yet implemented")
    }

    override suspend fun setSettings(settings: BoardSettings): Result<Unit> {
        TODO("Not yet implemented")

//        val request = Request(Method.POST, "/path")
//        return
//            .body(json.encodeToString(settings))
//        val response: Response = httpClient(request)

    }
}