package dev.tarkan.titlemanager.bukkit.api

import org.bukkit.plugin.Plugin

object TitleManagerServices {
    @JvmStatic
    fun get(plugin: Plugin): TitleManagerApi? {
        return plugin.server.servicesManager.load(TitleManagerApi::class.java)
    }

    @JvmStatic
    fun require(plugin: Plugin): TitleManagerApi {
        return requireNotNull(get(plugin)) { "TitleManager API is not registered." }
    }
}
