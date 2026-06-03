package dev.tarkan.titlemanager.bukkit.integration

import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import org.bukkit.scheduler.BukkitTask
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.concurrent.ConcurrentSkipListMap

class BungeeCordService(private val plugin: TitleManagerPlugin) : PluginMessageListener {
    private companion object {
        private const val CHANNEL = "BungeeCord"
        private const val GET_SERVER = "GetServer"
        private const val GET_SERVERS = "GetServers"
        private const val PLAYER_COUNT = "PlayerCount"
        private const val ALL_SERVERS = "ALL"
    }

    private var task: BukkitTask? = null
    private val serversByName = ConcurrentSkipListMap<String, BungeeCordServerInfo>(String.CASE_INSENSITIVE_ORDER)

    val currentServer: String?
        get() = currentServerName

    @Volatile
    private var currentServerName: String? = null

    val onlinePlayers: Int
        get() = serversByName.values.sumOf { it.playerCount }

    val servers: Map<String, BungeeCordServerInfo>
        get() = serversByName.toMap()

    fun playerCount(serverNames: String): String {
        return serverNames
            .split(',')
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .sumOf { serversByName[it]?.playerCount ?: 0 }
            .toString()
    }

    fun start() {
        plugin.server.messenger.registerOutgoingPluginChannel(plugin, CHANNEL)
        plugin.server.messenger.registerIncomingPluginChannel(plugin, CHANNEL, this)
        task = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            requestNetworkState()
        }, 0L, 200L)
    }

    fun close() {
        task?.cancel()
        task = null
        plugin.server.messenger.unregisterIncomingPluginChannel(plugin, CHANNEL, this)
        plugin.server.messenger.unregisterOutgoingPluginChannel(plugin, CHANNEL)
    }

    fun requestNetworkState() {
        sendNetworkMessage(GET_SERVERS)
        sendNetworkMessage(GET_SERVER)
    }

    fun sendNetworkMessage(vararg args: String, sender: Player? = plugin.server.onlinePlayers.firstOrNull()) {
        if (sender == null) {
            return
        }

        val payload = ByteArrayOutputStream().use { byteStream ->
            DataOutputStream(byteStream).use { output ->
                args.forEach(output::writeUTF)
            }
            byteStream.toByteArray()
        }

        sender.sendPluginMessage(plugin, CHANNEL, payload)
    }

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if (channel != CHANNEL) {
            return
        }

        DataInputStream(ByteArrayInputStream(message)).use { input ->
            when (input.readUTF()) {
                GET_SERVERS -> onGetServers(input.readUTF())
                GET_SERVER -> onGetServer(input.readUTF())
                PLAYER_COUNT -> onPlayerCount(input.readUTF(), input.readInt())
            }
        }
    }

    private fun onGetServers(serverNames: String) {
        val newServers = serverNames
            .split(", ")
            .filter { it.isNotBlank() }
            .toSet()

        serversByName.keys
            .filter { it !in newServers }
            .forEach(serversByName::remove)

        newServers
            .filter { it !in serversByName }
            .forEach { serversByName[it] = BungeeCordServerInfo(it) }

        newServers.forEach { sendNetworkMessage(PLAYER_COUNT, it) }
    }

    private fun onGetServer(serverName: String) {
        currentServerName = serverName
        serversByName.compute(serverName) { _, current ->
            (current ?: BungeeCordServerInfo(serverName)).copy(playerCount = plugin.server.onlinePlayers.size)
        }
    }

    private fun onPlayerCount(serverName: String, playerCount: Int) {
        if (serverName.equals(ALL_SERVERS, ignoreCase = true)) {
            return
        }

        serversByName.compute(serverName) { _, current ->
            (current ?: BungeeCordServerInfo(serverName)).copy(playerCount = playerCount)
        }
    }
}

data class BungeeCordServerInfo(
    val name: String,
    val playerCount: Int = 0
)
