package io.puharesource.mc.titlemanager.internal.reflections

import io.puharesource.mc.common.TitleManagerPlayer
import org.bukkit.entity.Player

class TitleManagerNmsRegistry(val version: String) {
    companion object {
        private const val BASE_PATH = "io.puharesource.mc.titlemanager.impl."
    }

    fun createPlayer(player: Player): TitleManagerPlayer<Player> = loadClass(args = arrayOf(player))

    private inline fun <reified T> loadClass(prefix: String = "", args: Array<Any?> = emptyArray()): T {
        val name = "${prefix}${T::class.java.simpleName}Impl"
        val clazz = Class.forName("$BASE_PATH$version.$name")
        val constructor = clazz.declaredConstructors.first()

        return constructor.newInstance(*args) as T
    }
}
