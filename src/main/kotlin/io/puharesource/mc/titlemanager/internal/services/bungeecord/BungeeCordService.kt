package io.puharesource.mc.titlemanager.internal.services.bungeecord

import io.puharesource.mc.titlemanager.internal.model.bungeecord.BungeeCordServerInfo
import org.bukkit.Bukkit
import org.bukkit.entity.Player

interface BungeeCordService {
    val onlinePlayers: Int
    val currentServer: String?
    val servers: Map<String, BungeeCordServerInfo>

    fun sendNetworkMessage(vararg args: String, sender: Player? = Bukkit.getOnlinePlayers().firstOrNull())

    fun start()
    fun stop()
}
