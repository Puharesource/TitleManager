package io.puharesource.mc.titlemanager.internal.services.bungeecord

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteStreams
import io.puharesource.mc.titlemanager.TitleManagerPlugin
import io.puharesource.mc.titlemanager.internal.model.bungeecord.BungeeCordChannels
import io.puharesource.mc.titlemanager.internal.model.bungeecord.BungeeCordMessages
import io.puharesource.mc.titlemanager.internal.model.bungeecord.BungeeCordServerInfo
import io.puharesource.mc.titlemanager.internal.model.event.TMPluginMessageListener
import io.puharesource.mc.titlemanager.internal.services.task.TaskService
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.ConcurrentSkipListMap
import javax.inject.Inject

class BungeeCordServiceSpigot @Inject constructor(private val plugin: TitleManagerPlugin, private val taskService: TaskService) : BungeeCordService {
    private val tasks: MutableSet<BukkitTask> = mutableSetOf()
    private var listener: TMPluginMessageListener? = null

    override val onlinePlayers: Int
        get() = servers.values.map { it.playerCount }.sum()

    override var currentServer: String? = null
        private set

    override var servers: MutableMap<String, BungeeCordServerInfo> = ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER)
        private set

    override fun sendNetworkMessage(vararg args: String, sender: Player?) {
        if (sender != null) {
            val output = ByteStreams.newDataOutput()

            args.forEach { output.writeUTF(it) }

            sender.sendPluginMessage(plugin, BungeeCordChannels.BUNGEECORD.channel, output.toByteArray())
        }
    }

    override fun start() {
        tasks.add(taskService.scheduleAsyncTimer(period = 200) {
            sendNetworkMessage(BungeeCordMessages.GET_SERVERS.message)
            sendNetworkMessage(BungeeCordMessages.GET_SERVER.message)
        })
        listener = TMPluginMessageListener(plugin) {
            if (it.channel != BungeeCordChannels.BUNGEECORD.channel) return@TMPluginMessageListener

            try {
                val message = it.message
                val input = ByteStreams.newDataInput(message)

                when (input.readUTF()) {
                    BungeeCordMessages.GET_SERVERS.message -> onGetServers(input)
                    BungeeCordMessages.GET_SERVER.message -> onGetServer(input)
                    BungeeCordMessages.PLAYER_COUNT.message -> onPlayerCount(input)
                }
            } catch (e: Exception) {
            }
        }
    }

    override fun stop() {
        tasks.forEach { it.cancel() }
        listener?.invalidate()
    }

    private fun onGetServers(input: ByteArrayDataInput) {
        val newServers = input.readUTF().split(", ").toSet()

        servers
                .filterKeys { !newServers.contains(it) }
                .forEach { servers.remove(it.key) }

        newServers.filter { !servers.containsKey(it) }.forEach { servers[it] = BungeeCordServerInfo(it, 0, -1) }

        servers.values.forEach { it.update(this) }
    }

    private fun onGetServer(input: ByteArrayDataInput) {
        val server = input.readUTF()
        currentServer = server

        if (!servers.containsKey(server)) {
            servers[server] = BungeeCordServerInfo(server, Bukkit.getOnlinePlayers().size, Bukkit.getMaxPlayers())
        } else {
            servers[server]?.playerCount = Bukkit.getOnlinePlayers().size
        }
    }

    private fun onPlayerCount(input: ByteArrayDataInput) {
        val server = input.readUTF()
        val playerCount = input.readInt()

        if (server.equals("ALL", ignoreCase = true)) return

        if (!servers.containsKey(server)) {
            servers[server] = BungeeCordServerInfo(server, playerCount, -1)
        } else {
            servers[server]?.playerCount = playerCount
        }
    }
}
