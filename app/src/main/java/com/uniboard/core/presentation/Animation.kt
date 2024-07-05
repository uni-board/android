package com.uniboard.core.presentation

import androidx.compose.animation.core.CubicBezierEasing

object Animation {
    object Easing {
        val emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        val emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
        val emphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    }
    object Duration {
        const val short1 = 50
        const val short2 = 100
        const val short3 = 150
        const val short4 = 200

        const val medium1 = 250
        const val medium2 = 300
        const val medium3 = 350
        const val medium4 = 400

        const val long1 = 450
        const val long2 = 500
        const val long3 = 550
        const val long4 = 600

        const val extraLong1 = 700
        const val extraLong2 = 800
        const val extraLong3 = 900
        const val extraLong4 = 1000
    }
}