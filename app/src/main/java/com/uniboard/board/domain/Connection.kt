package com.uniboard.board.domain

import kotlinx.coroutines.flow.StateFlow

interface Connection {
    val isConnected: StateFlow<Boolean>
    fun connect()
    fun disconnect()
}