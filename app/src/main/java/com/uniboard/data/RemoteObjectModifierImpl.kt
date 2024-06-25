package com.uniboard.data

import com.uniboard.domain.RemoteObject
import com.uniboard.domain.RemoteObjectModifier
import com.uniboard.domain.UObjectUpdate
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI

class RemoteObjectModifierImpl(
    private val baseURL: String,
    id: String,
    private val coroutineScope: CoroutineScope
) : RemoteObjectModifier {
    private val socket = run {
        val uri = URI.create(baseURL)
        val options = IO.Options.builder()
            .build()
        IO.socket(uri, options)
    }

    private val events = MutableSharedFlow<UObjectUpdate>(extraBufferCapacity = 100)

    init {
        socket.connect()
        socket.emit("connected", id)
        socket.on("created") { added ->
            coroutineScope.launch {
                events.emit(UObjectUpdate.Add(RemoteObject.toUObject(added[0].toString())))
            }
        }
        socket.on("modified") { modified ->
            coroutineScope.launch {
                val uobj = RemoteObject.toUObject(modified[0].toString())
                events.emit(
                    UObjectUpdate.Modify(
                        diff = uobj.state
                    )
                )
            }
        }
        socket.on("deleted") { deletedID ->
            coroutineScope.launch {
                events.emit(UObjectUpdate.Delete(deletedID[0].toString()))
            }
        }
    }

    override suspend fun send(update: UObjectUpdate): Result<Unit> = kotlin.runCatching {
        when (update) {
            is UObjectUpdate.Add -> socket.emit("created", update.obj.state.toString())
            is UObjectUpdate.Modify -> socket.emit(
                "modified",
                Json.encodeToString(update.diff)
            )
            is UObjectUpdate.Delete -> socket.emit("deleted", update.id)
        }
        Unit
    }.onFailure { it.printStackTrace() }

    override fun receive(): Flow<UObjectUpdate> {
        return events
    }
}