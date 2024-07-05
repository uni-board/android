@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalAnimationSpecApi::class)

package com.uniboard.core.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.SharedTransitionScope.PlaceHolderSize
import androidx.compose.animation.SharedTransitionScope.PlaceHolderSize.Companion.contentSize
import androidx.compose.animation.SharedTransitionScope.ResizeMode
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.ScaleToBounds
import androidx.compose.animation.core.ArcMode
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.ExperimentalAnimationSpecApi
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale

interface ContainerTransformScope {
    val transitionScope: SharedTransitionScope
    val animatedContentScope: AnimatedContentScope
}

@ExperimentalSharedTransitionApi
fun ContainerTransformScope(
    transitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) = object : ContainerTransformScope {
    override val transitionScope = transitionScope
    override val animatedContentScope = animatedContentScope
}

fun SharedTransitionScope.containerTransformScope(contentScope: AnimatedContentScope) =
    ContainerTransformScope(
        transitionScope = this,
        animatedContentScope = contentScope
    )

fun Modifier.sharedBounds(
    scope: ContainerTransformScope,
    key: Any,
    enter: EnterTransition = fadeIn(),
    exit: ExitTransition = fadeOut(),
    boundsTransform: BoundsTransform = BoundsTransform(),
    resizeMode: ResizeMode = ResizeMode.RemeasureToBounds,
    placeHolderSize: PlaceHolderSize = contentSize,
    renderInOverlayDuringTransition: Boolean = true,
    zIndexInOverlay: Float = 0f,
    shape: Shape? = null
) = composed {
    with(scope.transitionScope) {
        sharedBounds(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = scope.animatedContentScope,
            enter = enter,
            exit = exit,
            boundsTransform = boundsTransform,
            resizeMode = resizeMode,
            placeHolderSize = placeHolderSize,
            renderInOverlayDuringTransition = renderInOverlayDuringTransition,
            zIndexInOverlay = zIndexInOverlay,
            clipInOverlayDuringTransition = OverlayClip(
                shape ?: MaterialTheme.shapes.medium
            )
        )
    }
}

@OptIn(ExperimentalAnimationSpecApi::class)
fun BoundsTransform(
    arcMode: ArcMode = ArcMode.ArcAbove,
    easing: CubicBezierEasing = Animation.Easing.emphasized
): BoundsTransform =
    BoundsTransform { initialBounds: Rect, targetBounds: Rect ->
        keyframes {
            durationMillis = 500
            initialBounds at 0 using arcMode using easing
            targetBounds at 500
        }
    }

val DefaultBoundsTransform = androidx.compose.animation.BoundsTransform { _, _ -> tween() }