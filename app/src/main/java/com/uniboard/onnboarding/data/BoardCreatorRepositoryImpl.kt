package com.uniboard.onnboarding.data

import com.uniboard.board.data.to
import com.uniboard.onnboarding.domain.BoardCreatorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request

class BoardCreatorRepositoryImpl(private val handler: HttpHandler) : BoardCreatorRepository {
    override suspend fun createBoard(): Result<String> = withContext(Dispatchers.IO) {
        kotlin.runCatching {
            val response = handler(Request(Method.POST, "/createboard"))
            val id = response.to<JsonObject>()["id"]?.jsonPrimitive?.content
            requireNotNull(id)
        }
    }
}