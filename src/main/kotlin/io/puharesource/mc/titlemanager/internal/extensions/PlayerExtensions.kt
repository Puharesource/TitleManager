package io.puharesource.mc.titlemanager.internal.extensions

import io.puharesource.mc.titlemanager.internal.pluginInstance
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue

fun Player.getTitleManagerMetadata(key: String) = getMetadata(key).firstOrNull { it.owningPlugin == pluginInstance }

fun Player.setTitleManagerMetadata(key: String, any: Any) = setMetadata(key, FixedMetadataValue(pluginInstance, any))

fun Player.removeTitleManagerMetadata(key: String) = removeMetadata(key, pluginInstance)
