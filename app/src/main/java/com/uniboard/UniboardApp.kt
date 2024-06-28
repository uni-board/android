package com.uniboard

import android.app.Application
import com.uniboard.board.data.RootModuleImpl
import com.uniboard.board.domain.RootModule

class UniboardApp: Application() {
    val rootModule: RootModule by lazy { RootModuleImpl() }
}