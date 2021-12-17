package io.puharesource.mc.titlemanager.internal.services.placeholder

import io.puharesource.mc.titlemanager.internal.placeholder.Placeholder
import org.bukkit.entity.Player

interface PlaceholderService {
    fun loadBuiltinPlaceholders()

    fun addPlaceholder(placeholder: Placeholder)
    fun replaceText(player: Player, text: String): String

    fun containsPlaceholders(text: String): Boolean
    fun containsPlaceholder(text: String, placeholder: String): Boolean
}
