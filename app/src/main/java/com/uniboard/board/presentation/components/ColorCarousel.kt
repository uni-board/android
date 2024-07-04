package com.uniboard.board.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorCarousel(
    selectedColor: Color,
    onSelect: (Color) -> Unit,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(),
    colors: List<Color> =  defaultColors
) {
    val state = rememberCarouselState { colors.size }
    HorizontalUncontainedCarousel(
        state,
        modifier = modifier.fillMaxWidth(),
        itemWidth = 48.dp,
        itemSpacing = 4.dp,
        contentPadding = padding
    ) { index ->
        val color = colors[index]
        Box(
            Modifier
                .maskBorder(
                    BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.tertiary
                    ), CircleShape
                )
                .maskClip(CircleShape)
                .size(48.dp)
                .background(color)
                .clickable {
                    onSelect(color)
                    println("Selected")
                }, contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                color == selectedColor,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                Icon(
                    Icons.Default.Check, contentDescription = null,
                    tint = if (color.luminance() > 0.5f) Color.Black else Color.White
                )
            }
        }
    }
}




private val defaultColors = listOf(
    Color.Red,
    Color.Yellow,
    Color.Green,
    Color.Blue,
    Color.Cyan,
    Color.Black,
    Color.DarkGray,
    Color.Gray,
    Color.LightGray,
    Color.Magenta,
    Color.White
)