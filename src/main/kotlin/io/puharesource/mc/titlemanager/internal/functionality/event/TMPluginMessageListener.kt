package io.puharesource.mc.titlemanager.internal.functionality.event

import io.puharesource.mc.titlemanager.internal.onPluginDisable
import io.puharesource.mc.titlemanager.internal.pluginInstance
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener

class TMPluginMessageListener(val body: (PluginMessageReceivedItem) -> Unit) {
    private val listener: PluginMessageListener

    init {
        listener = PluginMessageListener { channel, player, message ->
            try {
                body(PluginMessageReceivedItem(channel, player, message))
            } catch (e: Exception) {}
        }

        pluginInstance.server.messenger.registerIncomingPluginChannel(pluginInstance, "BungeeCord", listener)

        onPluginDisable { invalidate() }
    }

    fun addTo(collection: MutableCollection<TMPluginMessageListener>) = collection.add(this)

    fun invalidate() = pluginInstance.server.messenger.unregisterIncomingPluginChannel(pluginInstance, "BungeeCord", listener)
}

class PluginMessageReceivedItem(var channel: String, var player: Player, var message: ByteArray)