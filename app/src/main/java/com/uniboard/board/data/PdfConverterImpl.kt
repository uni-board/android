package com.uniboard.board.data

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.uniboard.board.domain.PdfConverter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream


class PdfConverterImpl : PdfConverter {
    override fun convert(stream: InputStream): Sequence<ByteArray> = sequence {
        println("ALGKJAKJGKLAJGLK")
        val file = File.createTempFile("tmp", ".pdf")
        file.writeBytes(stream.readBytes())
        file.deleteOnExit()
        val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(descriptor)
        val pageCount = renderer.pageCount
        println(pageCount)
        for (i in 0 until pageCount) {
            val page = renderer.openPage(i)
            println(page)
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
}