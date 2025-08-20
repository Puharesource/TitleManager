package studio.minekarta.titlemanagerreborn.internal.services.bungeecord

import studio.minekarta.titlemanagerreborn.internal.model.bungeecord.BungeeCordServerInfo
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
