package com.uniboard.board.data

import org.http4k.core.Body
import org.http4k.core.Response
import org.http4k.format.KotlinxSerialization.auto

inline fun <reified T: Any> Response.to() = Body.auto<T>().toLens()(this)