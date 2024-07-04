package com.uniboard.board.presentation.components

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import com.uniboard.board.domain.RootModule
import com.uniboard.board.presentation.UiUObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.absoluteValue

@SuppressLint("RestrictedApi")
@Composable
fun RootModule.PdfObject(obj: UiUObject, modifier: Modifier = Modifier) {
    val pdfId = remember(obj) {
        requireNotNull(obj.state["uniboardData"]?.jsonObject?.get("data")?.jsonPrimitive?.content)
    }
    val images by produceState(listOf<ImageBitmap>()) {
        withContext(Dispatchers.IO) {
            val stream = fileDownloader.download(pdfId)
            pdfConverter.convert(stream).forEach { bytes ->
                println(value)
                value += BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
            }
        }
    }
    val pager = rememberPagerState { Int.MAX_VALUE }
    HorizontalPager(
        pager,
        modifier,
        userScrollEnabled = !obj.editable,
        pageSpacing = 40.dp
    ) { absoluteIndex ->
        if (images.isNotEmpty()) {
            val index = absoluteIndex % images.size
            val bitmap = images[index % images.size]
            Box(Modifier
                .graphicsLayer {
                    val pageOffset =
                        (pager.currentPage - absoluteIndex) + pager
                            .currentPageOffsetFraction

                    alpha = lerp(
                        start = 0.4f,
                        stop = 1f,
                        amount = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f),
                    )

                    cameraDistance = 4 * density
                    rotationY = lerp(
                        start = 0f,
                        stop = 40f,
                        amount = pageOffset.coerceIn(-1f, 1f),
                    )

                    lerp(
                        start = 0.5f,
                        stop = 1f,
                        amount = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f),
                    ).also { scale ->
                        scaleX = scale
                        scaleY = scale
                    }
                }
                .clip(MaterialTheme.shapes.extraLarge)
                .background(Color.White)
                .fillMaxSize()) {
                Image(
                    bitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = index.toString(),
                    Modifier
                        .padding(24.dp)
                        .align(Alignment.BottomEnd)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
