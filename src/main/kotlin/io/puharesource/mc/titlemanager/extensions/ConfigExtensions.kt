package io.puharesource.mc.titlemanager.extensions

import com.google.common.base.Joiner
import org.bukkit.configuration.ConfigurationSection

fun ConfigurationSection.getStringWithMultilines(path: String) : String {
    val value = get(path)

    if (value is String) {
        return value
    } else if (value is List<*>) {
        return Joiner.on('\n').join(getStringList(path))
    }

    return ""
}