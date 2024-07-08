package com.uniboard.board.data

import android.content.Context
import com.uniboard.board_details.domain.BoardSettingsRepository
import com.uniboard.board.domain.FileRepository
import com.uniboard.board.domain.RemoteObjectModifier
import com.uniboard.board.domain.RemoteObjectRepository
import com.uniboard.board.domain.RootModule
import com.uniboard.onnboarding.data.BoardCreatorRepositoryImpl
import com.uniboard.onnboarding.data.RecentBoardsRepositoryImpl
import com.uniboard.board_details.data.BoardSettingsRepositoryImpl
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.http4k.client.ApacheClient
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters

class RootModuleImpl(context: Context): RootModule {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    })
    override val baseUrl = "https://api.uniboard-api.freemyip.com"
    private val httpClient = ClientFilters.SetBaseUriFrom(Uri.of(baseUrl)).then(ApacheClient())
    override fun remoteObjectRepository(id: String): RemoteObjectRepository {
        return RemoteObjectRepositoryImpl(id, httpClient)
    }

    override fun remoteObjectModifier(id: String): RemoteObjectModifier {
        return RemoteObjectModifierImpl(baseUrl, id, coroutineScope)
    }

    override val fileRepository: FileRepository by lazy {
        FileRepositoryImpl(context, baseUrl, httpClient)
    }
    override val pdfRenderer by lazy {
        PdfRendererImpl()
    }
    override val boardCreatorRepository by lazy {
        BoardCreatorRepositoryImpl(httpClient)
    }
    override val recentBoardsRepository by lazy {
        RecentBoardsRepositoryImpl(context)
    }
    override fun boardSettingsRepository(id: String): BoardSettingsRepository {
        return BoardSettingsRepositoryImpl(id, httpClient)
    }

}