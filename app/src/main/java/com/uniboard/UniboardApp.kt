package com.uniboard

import android.app.Application
import com.uniboard.data.RootModuleImpl
import com.uniboard.domain.RootModule

class UniboardApp: Application() {
    val rootModule: RootModule by lazy { RootModuleImpl() }
}