package com.uniboard.board.data

import com.uniboard.board.domain.RemoteObject
import com.uniboard.board.domain.RemoteObjectRepository
import com.uniboard.board.domain.UObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response

class RemoteObjectRepositoryImpl(
    private val id: String,
    private val httpClient: HttpHandler,
) : RemoteObjectRepository {
    override suspend fun allObjects(): Result<List<UObject>> = withContext(Dispatchers.IO) {
        kotlin.runCatching {
            val request = Request(Method.GET, "/board/$id/get")
            val response: Response = httpClient(request)
            RemoteObject.toUObjectList(response.to<JsonArray>())
        }.onFailure { it.printStackTrace() }
    }
}