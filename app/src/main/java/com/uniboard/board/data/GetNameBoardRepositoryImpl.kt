package com.uniboard.board.data

import com.uniboard.board.domain.GetNameBoardRepository
import com.uniboard.board.domain.RemoteObject
import com.uniboard.board.domain.UObject
import com.uniboard.board.domain.RemoteObjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.format.KotlinxSerialization.asJsonObject


class GetNameBoardRepositoryImpl(
    private val id: String,
    private val httpClient: HttpHandler,
) : GetNameBoardRepository {
    override suspend fun getName(id: String): String {
        var f = withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val request = Request(Method.GET, "/board/$id/get")
                val response: Response = httpClient(request)
                RemoteObject.toUObjectList(response.to<JsonArray>())
            }.onFailure { it.printStackTrace() }
        }
        return f.toString()

    }
}
