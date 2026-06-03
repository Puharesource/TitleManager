package dev.tarkan.titlemanager.bukkit.extensions

import dev.tarkan.titlemanager.api.TitleManagerCoreApi
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.TextColor

fun String.splitTypedLineBreak(limit: Int = 0) = TitleManagerCoreApi.splitTypedLineBreak(this, limit).toList()

fun String.color(): String = TitleManagerCoreApi.translateLegacyColorCodes(this)

fun Iterable<String>.joinToComponent(separator: String, color: TextColor = MessageColors.SECONDARY, separatorColor: TextColor = MessageColors.PRIMARY) =
    Component.join(JoinConfiguration.separator(Component.text(separator).color(separatorColor)), this.map { Component.text(it).color(color) })