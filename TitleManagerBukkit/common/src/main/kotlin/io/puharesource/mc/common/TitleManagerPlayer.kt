package io.puharesource.mc.common

import java.util.UUID

abstract class TitleManagerPlayer<T>(val handle: T) {
    abstract val id: UUID
    abstract val ping: Int

    abstract var playerListHeader: String
    abstract var playerListFooter: String

    abstract fun sendActionbarMessage(message: String)

    abstract fun sendTitle(message: String, fadeIn: Int = 10, stay: Int = 70, fadeOut: Int = 20)
    abstract fun sendSubtitle(message: String, fadeIn: Int = 10, stay: Int = 70, fadeOut: Int = 20)
    abstract fun sendTitles(title: String, subtitle: String, fadeIn: Int = 10, stay: Int = 70, fadeOut: Int = 20)
    abstract fun sendTitleTimings(fadeIn: Int, stay: Int, fadeOut: Int)
    abstract fun resetTitle()
}
