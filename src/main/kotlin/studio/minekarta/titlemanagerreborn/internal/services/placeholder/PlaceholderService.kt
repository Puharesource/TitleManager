package studio.minekarta.titlemanagerreborn.internal.services.placeholder

import studio.minekarta.titlemanagerreborn.internal.placeholder.Placeholder
import org.bukkit.entity.Player

interface PlaceholderService {
    fun loadBuiltinPlaceholders()

    fun addPlaceholder(placeholder: Placeholder)
    fun replaceText(player: Player, text: String): String

    fun containsPlaceholders(text: String): Boolean
    fun containsPlaceholder(text: String, placeholder: String): Boolean
}
