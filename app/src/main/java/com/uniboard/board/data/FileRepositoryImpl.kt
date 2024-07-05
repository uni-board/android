package com.uniboard.board.data

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.uniboard.board.domain.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.MultipartFormBody
import org.http4k.core.Request
import org.http4k.lens.MultipartFormFile
import java.io.InputStream

class FileRepositoryImpl(
    private val context: Context,
    private val baseUrl: String,
    private val handler: HttpHandler
) : FileRepository {
    override fun download(id: String): InputStream {
        val request = Request(Method.GET, "/storage/$id")
        return handler(request).body.stream
    }

    override suspend fun upload(stream: InputStream): Result<String> = kotlin.runCatching {
        withContext(Dispatchers.IO) {
            val body = MultipartFormBody().plus(
                "file" to MultipartFormFile(
                    "file",
                    ContentType.OCTET_STREAM,
                    stream
                )
            )
            val request = Request(Method.POST, "/storage/add")
                .body(body)
                .header("content-type", "multipart/form-data; boundary=${body.boundary}")
            val response = handler(request)
            println(response.status)
            response.to<JsonObject>()["id"]?.jsonPrimitive?.content!!
        }
    }.onFailure { it.printStackTrace() }

    override fun downloadToDevice(id: String, name: String) {
        downloadFile(name, name, "$baseUrl/storage/$id")
    }

    private fun downloadFile(fileName: String, desc: String, url: String) {
        // fileName -> fileName with extension
        val request = DownloadManager.Request(Uri.parse(url))
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setTitle(fileName)
            .setDescription(desc)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadID = downloadManager.enqueue(request)
    }
}