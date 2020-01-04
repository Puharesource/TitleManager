package io.puharesource.mc.titlemanager.internal.extensions

import io.puharesource.mc.titlemanager.internal.pluginInstance
import org.bukkit.command.CommandSender

internal fun CommandSender.sendConfigMessage(path: String, vararg replace: Pair<String, String>) {
    var replacedMessage = pluginInstance.config.getString("messages.$path")
            .orEmpty()
            .color()
            .replace("\\n", "\n")

    val replaceMap = mapOf(*replace)

    replaceMap.entries.forEach {
        replacedMessage = replacedMessage.replace("%${it.key}", it.value)
    }

    sendMessage(replacedMessage)
}