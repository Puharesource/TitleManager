package io.puharesource.mc.titlemanager.internal.extensions

import net.md_5.bungee.api.ChatColor

internal fun String.color(): String = ChatColor.translateAlternateColorCodes('&', this)

internal fun String.stripColor(): String = ChatColor.stripColor(this)!!

internal fun String.isInt(): Boolean {
    return try {
        toInt()
        true
    } catch (e: NumberFormatException) {
        false
    }
}

internal fun String.isDouble(): Boolean {
    return try {
        toDouble()
        true
    } catch (e: NumberFormatException) {
        false
    }
}
