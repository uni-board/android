package com.uniboard

import android.os.Bundle
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.ExperimentalAnimationSpecApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.uniboard.board.domain.RootModule
import com.uniboard.board.presentation.BoardDestination
import com.uniboard.board.presentation.BoardNavigationEvent
import com.uniboard.board.presentation.BoardScreen
import com.uniboard.board_details.presentation.BoardDetailsDestination
import com.uniboard.board_details.presentation.BoardDetailsFragment
import com.uniboard.core.presentation.AndroidNavigationFragment
import com.uniboard.core.presentation.ContainerTransformScope
import com.uniboard.core.presentation.DefaultBoundsTransform
import com.uniboard.core.presentation.NavigationFragment
import com.uniboard.core.presentation.containerTransformScope
import com.uniboard.core.presentation.sharedBounds
import com.uniboard.help.presentation.DetailsHelpInfo
import com.uniboard.help.presentation.HelpDestination
import com.uniboard.help.presentation.HelpDetailsDestination
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


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RootModule.Navigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    SharedTransitionLayout(modifier) {
        NavHost(
            navController = navController,
            // Change this to other destination(e.g. OnboardingDestination) to change the start destination
            startDestination = BoardDestination("35aaf09c-718f-40fd-86df-f46302ad289d")
        ) {
            composable<BoardDestination> {
                val data: BoardDestination = it.toRoute()
                BoardScreen(
                    id = data.id,
                    transitionScope = containerTransformScope(this),
                    onNavigate = { event ->
                        when (event) {
                            is BoardNavigationEvent.GoToDetails -> navController.navigate(
                                BoardDetailsDestination(data.id)
                            )

                            is BoardNavigationEvent.GoToHelp -> navController.navigate(
                                HelpDestination
                            )

                            is BoardNavigationEvent.Quit -> navController.navigate(
                                OnboardingDestination
                            ) // TODO: Reset root to Onboarding
                        }
                    },
                    modifier = modifier
                )
            }
            composable<OnboardingDestination> {
                AnimatedNavigationFragment<OnboardingFragment>(
                    navController = navController,
                    arguments = it.arguments,
                    key = OnboardingDestination,
                    scope = containerTransformScope(this)
                )
            }
            composable<HelpDestination> {
                AnimatedNavigationFragment<HelpFragment>(
                    navController = navController,
                    scope = containerTransformScope(this),
                    arguments = it.arguments,
                    key = HelpDestination
                )
            }
            composable<HelpDetailsDestination> {
                AnimatedNavigationFragment<DetailsHelpInfo>(
                    navController = navController,
                    scope = containerTransformScope(this),
                    arguments = it.arguments,
                    key = HelpDetailsDestination
                )
            }
            composable<BoardDetailsDestination> {
                AnimatedNavigationFragment<BoardDetailsFragment>(
                    navController = navController,
                    arguments = it.arguments,
                    scope = containerTransformScope(this),
                    key = BoardDetailsDestination,
                    boundsTransform = DefaultBoundsTransform
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalAnimationSpecApi::class)
@Composable
private inline fun <reified T : NavigationFragment> AnimatedNavigationFragment(
    navController: NavController,
    scope: ContainerTransformScope,
    key: Any,
    modifier: Modifier = Modifier,
    arguments: Bundle? = null,
    boundsTransform: BoundsTransform = com.uniboard.core.presentation.BoundsTransform()
) {
    AndroidNavigationFragment<T>(
        navController, arguments = arguments,
        modifier = modifier
            .safeDrawingPadding()
            .padding(16.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .sharedBounds(
                scope,
                key = key,
                shape = MaterialTheme.shapes.extraLarge,
                boundsTransform = boundsTransform
            )
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxSize()
    )
}