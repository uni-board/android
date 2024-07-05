package com.uniboard.board.data

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.ui.geometry.Size
import com.uniboard.util.toByteArray
import java.io.File
import java.io.InputStream


class PdfRendererImpl : com.uniboard.board.domain.PdfRenderer {
    override fun convert(stream: InputStream): Sequence<ByteArray> = pagesFrom(stream).map { page ->
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        bitmap.toByteArray()
    }

    private fun pagesFrom(stream: InputStream) = sequence<PdfRenderer.Page> {
        val file = File.createTempFile("tmp", ".pdf")
        file.writeBytes(stream.readBytes())
        file.deleteOnExit()
        val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(descriptor)
        val pageCount = renderer.pageCount
        for (i in 0 until pageCount) {
            val page = renderer.openPage(i)
            yield(page)
            page.close()
        }
        renderer.close()
    }

    override fun measureSize(stream: InputStream): Size = pagesFrom(stream).first().run {
        Size(width.toFloat(), height.toFloat())
    }
}