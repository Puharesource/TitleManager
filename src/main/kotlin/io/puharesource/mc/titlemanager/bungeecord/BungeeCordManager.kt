package io.puharesource.mc.titlemanager.bungeecord

import com.google.common.io.ByteStreams
import io.puharesource.mc.titlemanager.event.observePluginMessageReceived
import io.puharesource.mc.titlemanager.pluginConfig
import io.puharesource.mc.titlemanager.pluginInstance
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import io.puharesource.mc.titlemanager.scheduling.scheduleAsyncObservableTimer
import java.util.concurrent.ConcurrentSkipListMap

object BungeeCordManager {
    private val servers: MutableMap<String, ServerInfo> = ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER)
    private var currentServer: String? = null

    val onlinePlayers: Int
        get() = servers.values.map { it.playerCount }.sum()

    init {
        scheduleAsyncObservableTimer(period = 200)
                .filter { pluginConfig.usingBungeecord }
                .subscribe {
                    sendNetworkMessage("GetServers")
                    sendNetworkMessage("GetServer")
                }

        observePluginMessageReceived()
                .filter { pluginConfig.usingBungeecord }
                .onErrorResumeNext { null }
                .filter { it != null }
                .filter { it.channel == "BungeeCord" }
                .subscribe { pluginMessage ->
                    try {
                        val message = pluginMessage.message

                        val input = ByteStreams.newDataInput(message)

                        when (input.readUTF()) {
                            "GetServers" -> {
                                val newServers = input.readUTF().split(", ").toSet()

                                servers
                                        .filterKeys { !newServers.contains(it) }
                                        .forEach { servers.remove(it.key) }

                                newServers.filter { !servers.containsKey(it) }.forEach { servers[it] = ServerInfo(it, 0, -1) }

                                servers.values.forEach { it.update() }
                            }
                            "GetServer" -> {
                                val server = input.readUTF()
                                currentServer = server

                                if (!servers.containsKey(server)) {
                                    servers[server] = ServerInfo(server, Bukkit.getOnlinePlayers().size, Bukkit.getMaxPlayers())
                                } else {
                                    servers[server]?.playerCount = Bukkit.getOnlinePlayers().size
                                }
                            }
                            "PlayerCount" -> {
                                val server = input.readUTF()
                                val playerCount = input.readInt()

                                if (server.equals("ALL", ignoreCase = true)) return@subscribe

                                if (!servers.containsKey(server)) {
                                    servers[server] = ServerInfo(server, playerCount, -1)
                                } else {
                                    servers[server]?.playerCount = playerCount
                                }
                            }
                        }
                    } catch (e: Exception) {}
                }
    }

    fun getCurrentServer() = currentServer

    fun getServers() = servers

    fun sendNetworkMessage(vararg args: String, sender: Player? = Bukkit.getOnlinePlayers().firstOrNull()) {
        if (sender != null) {
            val output = ByteStreams.newDataOutput()

            args.forEach { output.writeUTF(it) }

            sender.sendPluginMessage(pluginInstance, "BungeeCord", output.toByteArray())
        }
    }
}

data class ServerInfo(val name: String, var playerCount: Int = 0, var maxPlayers: Int = 0) {
    fun update() = BungeeCordManager.sendNetworkMessage("PlayerCount", name)
}