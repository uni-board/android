package com.uniboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.uniboard.board.domain.RootModule
import com.uniboard.board.presentation.BoardDestination
import com.uniboard.board.presentation.BoardScreen
import com.uniboard.board_details.presentation.BoardDetailsDestination
import com.uniboard.board_details.presentation.BoardDetailsFragment
import com.uniboard.core.presentation.AndroidNavigationFragment
import com.uniboard.help.presentation.HelpDestination
import com.uniboard.help.presentation.HelpFragment
import com.uniboard.onnboarding.presentation.OnboardingDestination
import com.uniboard.onnboarding.presentation.OnboardingFragment


@Composable
fun App(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val module = remember { (context.applicationContext as UniboardApp).rootModule }
    with(module) {
        Navigation(modifier)
    }
}

@Composable
private fun RootModule.Navigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        // Change this to other destination(e.g. OnboardingDestination) to change the start destination
        startDestination = BoardDestination("f67e6113-370f-456a-9e7f-9904f9c710d2"),
        modifier = modifier
    ) {
        composable<BoardDestination> {
            val data: BoardDestination = it.toRoute()
            BoardScreen(id = data.id, modifier = modifier)
        }
        composable<OnboardingDestination> {
            AndroidNavigationFragment<OnboardingFragment>(navController, arguments = it.arguments, modifier = Modifier.fillMaxSize())
        }
        composable<HelpDestination> {
            AndroidNavigationFragment<HelpFragment>(navController, arguments = it.arguments)
        }
        composable<BoardDetailsDestination> {
            AndroidNavigationFragment<BoardDetailsFragment>(navController, arguments = it.arguments)
        }
    }
}