package com.uniboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.uniboard.board.presentation.board.BoardScreen

@Composable
fun App(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val module = remember { (context.applicationContext as UniboardApp).rootModule }
    with(module) {
        BoardScreen(id = "f67e6113-370f-456a-9e7f-9904f9c710d2", modifier = modifier)
    }
}