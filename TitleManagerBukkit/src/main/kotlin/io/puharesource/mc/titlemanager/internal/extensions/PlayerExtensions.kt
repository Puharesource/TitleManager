package io.puharesource.mc.titlemanager.internal.extensions

import io.puharesource.mc.common.TitleManagerPlayer
import io.puharesource.mc.titlemanager.internal.pluginInstance
import io.puharesource.mc.titlemanager.internal.reflections.NMSManager
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue

fun Player.getTitleManagerPlayer(): TitleManagerPlayer<Player> {
    var metadata = getMetadata("TITLEMANAGER_PLAYER").firstOrNull { it.owningPlugin == pluginInstance }

    if (metadata == null) {
        val player = NMSManager.registry.createPlayer(this)
        metadata = FixedMetadataValue(pluginInstance, player)

        setMetadata("TITLEMANAGER_PLAYER", metadata)
    }

    return metadata.value() as TitleManagerPlayer<Player>
}
