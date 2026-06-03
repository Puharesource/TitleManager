package dev.tarkan.titlemanager.bukkit.command

import net.kyori.adventure.text.Component

data class CommandSuggestion(
    val text: String,
    val tooltip: Component? = null
) {
    companion object {
        fun text(text: String): CommandSuggestion = CommandSuggestion(text)
    }
}
