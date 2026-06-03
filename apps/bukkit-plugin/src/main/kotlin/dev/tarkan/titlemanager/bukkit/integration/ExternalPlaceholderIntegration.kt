package dev.tarkan.titlemanager.bukkit.integration

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class ExternalPlaceholderIntegration(private val plugin: JavaPlugin) {
    private companion object {
        private const val PLACEHOLDER_API_PLUGIN = "PlaceholderAPI"
        private const val PLACEHOLDER_API_CLASS = "me.clip.placeholderapi.PlaceholderAPI"
    }

    private val isPlaceholderApiAvailable = isClassPresent(PLACEHOLDER_API_CLASS)

    fun replace(player: Player, text: String): String {
        if (!isPlaceholderApiAvailable || !plugin.server.pluginManager.isPluginEnabled(PLACEHOLDER_API_PLUGIN)) {
            return text
        }

        return PlaceholderAPI.setPlaceholders(player, text)
    }

    private fun isClassPresent(className: String): Boolean {
        return try {
            Class.forName(className, false, plugin.javaClass.classLoader)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }
}
