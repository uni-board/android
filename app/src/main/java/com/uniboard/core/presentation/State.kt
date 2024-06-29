package com.uniboard.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
inline fun <T> rememberState(crossinline initializer: @DisallowComposableCalls () -> T): MutableState<T> {
    return remember { mutableStateOf(initializer()) }
}

@Composable
inline fun <T> rememberState(key: Any?, crossinline initializer: @DisallowComposableCalls () -> T): MutableState<T> {
   val state = remember { mutableStateOf(initializer()) }
    LaunchedEffect(key) {
        state.value = initializer()
    }
    return state
}

@Composable
inline fun <T> rememberState(key1: Any?, key2: Any?, crossinline initializer: @DisallowComposableCalls () -> T): MutableState<T> {
    val state = remember { mutableStateOf(initializer()) }
    LaunchedEffect(key1, key2) {
        state.value = initializer()
    }
    return state
}

@Composable
inline fun <T> rememberState(key1: Any?, key2: Any?, key3: Any?, crossinline initializer: @DisallowComposableCalls () -> T): MutableState<T> {
    val state = remember { mutableStateOf(initializer()) }
    LaunchedEffect(key1, key2, key3) {
        state.value = initializer()
    }
    return state
}

@Composable
inline fun <T> rememberState(vararg keys: Any?, crossinline initializer: @DisallowComposableCalls () -> T): MutableState<T> {
    val state = remember { mutableStateOf(initializer()) }
    LaunchedEffect(*keys) {
        state.value = initializer()
    }
    return state
}