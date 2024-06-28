package com.uniboard.board.data

import com.uniboard.board.domain.RemoteObject
import com.uniboard.board.domain.RemoteObjectModifier
import com.uniboard.board.domain.UObjectUpdate
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
                println(added[0])
                events.emit(UObjectUpdate.Add(RemoteObject.toUObject(added[0].toString())))
            }
        }
        socket.on("modified") { modified ->
            coroutineScope.launch {
                val uobj = RemoteObject.toUObject(modified[0].toString())
                println(uobj)
                events.emit(
                    UObjectUpdate.Modify(
                        diff = uobj.state
                    )
                )
            }
        }
        socket.on("deleted") { deletedID ->
            coroutineScope.launch {
                println(deletedID)
                events.emit(UObjectUpdate.Delete(deletedID[0].toString()))
            }
        }
    }

    override suspend fun send(update: UObjectUpdate): Result<Unit> = kotlin.runCatching {
        println(update)
        when (update) {
            is UObjectUpdate.Add -> {
                socket.emit("created", Json.encodeToString(update.obj.state))
                events.emit(UObjectUpdate.Add(update.obj))
            }

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