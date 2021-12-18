package io.puharesource.mc.common

import java.util.UUID

abstract class TitleManagerPlayer<T>(val handle: T) {
    abstract val id: UUID
    abstract val ping: Int

    abstract fun sendActionbarMessage(message: String)
}
