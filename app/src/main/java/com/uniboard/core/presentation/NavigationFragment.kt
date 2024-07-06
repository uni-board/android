package com.uniboard.core.presentation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.fragment.compose.AndroidFragment
import androidx.navigation.NavController

@Composable
inline fun <reified T : NavigationFragment> AndroidNavigationFragment(
    navController: NavController,
    modifier: Modifier = Modifier,
    arguments: Bundle? = null,
    noinline onUpdate: T.() -> Unit = {}
) {
    AndroidFragment<T>(modifier = modifier, arguments = arguments ?: Bundle.EMPTY) {
        it.navController = navController
        onUpdate(it)
    }
}

abstract class NavigationFragment(layoutId: Int) : Fragment(layoutId) {
    lateinit var navController: NavController
}