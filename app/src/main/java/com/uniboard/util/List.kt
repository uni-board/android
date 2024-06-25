package com.uniboard.util

fun <K, V> Map<K, V>.mutate(block: MutableMap<K, V>.() -> Unit): Map<K, V> {
    return toMutableMap().apply(block)
}

fun <K, V> Map<K, V>.diffWith(other: Map<K, V>): Map<K, V> {
    return filter { (key, value) -> other[key] != value }
}