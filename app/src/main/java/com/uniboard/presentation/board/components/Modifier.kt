package com.uniboard.presentation.board.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round
import com.uniboard.presentation.board.BoardScreenEvent
import com.uniboard.presentation.board.UiUObject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun Modifier.transformable(
    obj: UiUObject,
    enabled: Boolean = true,
    onEnd: (scaleX: Float, scaleY: Float, rotation: Float, offset: IntOffset) -> Unit
) = composed {
    var scaleX by remember(obj.scaleX) { mutableFloatStateOf(obj.scaleX) }
    var scaleY by remember(obj.scaleY) { mutableFloatStateOf(obj.scaleY) }
    var rotation by remember(obj.angle) { mutableFloatStateOf(obj.angle) }
    var offset by remember(obj.left, obj.top) {
        println("Created new offset")
        mutableStateOf(
            Offset(
                obj.left.toFloat(),
                obj.top.toFloat()
            )
        )
    }
    layout { measurable, constraints ->
        val width = obj.width ?: constraints.maxWidth
        val height = obj.height ?: constraints.maxHeight
        val top = offset.y
        val left = offset.x
        val placeable = measurable.measure(
            Constraints(
                minWidth = width,
                minHeight = height,
                maxWidth = width,
                maxHeight = height
            )
        )
        layout(width, height) {
            placeable.placeWithLayer(left.toInt(), top.toInt()) {
                rotationZ = rotation
                this.scaleX = scaleX
                this.scaleY = scaleY
                transformOrigin = TransformOrigin(0f, 0f)
            }
        }
    }.then(if (enabled) Modifier
        .pointerInput(Unit) {
            detectTransformGestures(
                onGesture = { centroid, panChange, zoomChange, rotationChange ->
                    scaleX *= zoomChange
                    scaleY *= zoomChange
                    rotation += rotationChange
                    val newOffset = panChange.rotateBy(rotation)
                    offset += Offset(newOffset.x * scaleX, newOffset.y * scaleY)

                    println(scaleX)
                    println(scaleY)
                    println(rotation)
                    println(offset)
                }
            )
        }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                do {
                    val event = awaitPointerEvent()
                    val canceled =
                        event.changes.any { it.isConsumed }
                } while (!canceled && event.changes.any { it.pressed })
                onEnd(scaleX, scaleY, rotation, offset.round())
            }
        } else Modifier)

}

private fun Offset.rotateBy(angle: Float): Offset {
    val angleInRadians = angle * PI / 180
    return Offset(
        (x * cos(angleInRadians) - y * sin(angleInRadians)).toFloat(),
        (x * sin(angleInRadians) + y * cos(angleInRadians)).toFloat()
    )
}
