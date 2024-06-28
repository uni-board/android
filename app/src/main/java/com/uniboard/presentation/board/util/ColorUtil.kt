package com.uniboard.presentation.board.util

import androidx.compose.ui.graphics.Color

fun String.parseAsRGBAColor(): Color {
    if (this == "transparent") return Color.Transparent
    val (r, g, b, a) = replace("rgba(", "").replace(")", "").split(",")
        .map { it.trim().toFloat() }
    return Color(r / 255f, g / 255f, b / 255f, a)
}

fun Color.asCSSString(): String {
    return "rgba(${red * 255}, ${green * 255}, ${blue * 255}, $alpha)"
}