package dev.tarkan.titlemanager.bukkit.extensions

val Boolean.toggleText: String
    get() = if (this) "on" else "off"