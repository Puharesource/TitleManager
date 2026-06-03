package dev.tarkan.titlemanager.bukkit.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

object ComponentSerializer {
    private val serializer = LegacyComponentSerializer.legacy('§')

    fun deserialize(input: String): TextComponent {
        return serializer.deserialize(input)
    }

    fun serialize(component: Component): String {
        return serializer.serialize(component)
    }
}