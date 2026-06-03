package dev.tarkan.titlemanager.bukkit.extensions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.command.CommandSender

private val legacyComponentSerializer = LegacyComponentSerializer.legacySection()

fun Component.toLegacyText(): String = legacyComponentSerializer.serialize(this)

fun CommandSender.sendTitleManagerMessage(message: Component) {
    sendMessage(message.toLegacyText())
}
