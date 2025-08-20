package studio.minekarta.titlemanagerreborn.internal.model.event

import org.bukkit.entity.Player

class PluginMessageReceivedItem(var channel: String, var player: Player, var message: ByteArray)
