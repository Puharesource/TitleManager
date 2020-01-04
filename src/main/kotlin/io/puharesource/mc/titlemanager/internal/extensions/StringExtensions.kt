package io.puharesource.mc.titlemanager.internal.extensions

import org.bukkit.ChatColor

internal fun String.color() : String = ChatColor.translateAlternateColorCodes('&', this)

internal fun String.stripColor() : String = ChatColor.stripColor(this)!!

internal fun String.isInt() : Boolean {
    return try {
        toInt()
        true
    } catch (e: NumberFormatException) {
        false
    }
}

internal fun String.isDouble() : Boolean {
    return try {
        toDouble()
        true
    } catch (e: NumberFormatException) {
        false
    }
}