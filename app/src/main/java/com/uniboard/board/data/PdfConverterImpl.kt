package com.uniboard.board.data

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.uniboard.board.domain.PdfConverter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream


class PdfConverterImpl : PdfConverter {
    override fun convert(stream: InputStream): Sequence<ByteArray> = sequence {
        val descriptor = getFileDescriptor(stream.readBytes())
        val renderer = PdfRenderer(descriptor)

        val pageCount = renderer.pageCount
        for (i in 0 until pageCount) {
            val page = renderer.openPage(i)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            yield(bitmap.toByteArray())
        }
        renderer.close()
    }

    private fun Bitmap.toByteArray(): ByteArray {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 90, stream)
        return stream.toByteArray()
    }

    private fun getFileDescriptor(fileData: ByteArray): ParcelFileDescriptor {
        val pipe = ParcelFileDescriptor.createPipe()

        val inputStream = ByteArrayInputStream(fileData)
        val outputStream =
            ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])
        var len: Int
        while ((inputStream.read().also { len = it }) >= 0) {
            outputStream.write(len)
        }
        inputStream.close()
        outputStream.flush()
        outputStream.close()
        return pipe[0]
    }
}