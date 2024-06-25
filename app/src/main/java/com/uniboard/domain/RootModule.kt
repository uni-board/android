package com.uniboard.domain

interface RootModule {
    fun remoteObjectRepository(id: String): RemoteObjectRepository
    fun remoteObjectModifier(id: String): RemoteObjectModifier
}