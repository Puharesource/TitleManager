package io.puharesource.mc.titlemanager.internal.model.event

import org.bukkit.entity.Player

class PluginMessageReceivedItem(var channel: String, var player: Player, var message: ByteArray)
