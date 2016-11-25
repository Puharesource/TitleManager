package io.puharesource.mc.titlemanager.extensions

import org.bukkit.ChatColor

internal fun String.color() : String = ChatColor.translateAlternateColorCodes('&', this)

internal fun String.stripColor() : String = ChatColor.stripColor(this)

internal fun String.isInt() : Boolean {
    try {
        toInt()
        return true
    } catch (e: NumberFormatException) {
        return false
    }
}

internal fun String.isDouble() : Boolean {
    try {
        toDouble()
        return true
    } catch (e: NumberFormatException) {
        return false
    }
}