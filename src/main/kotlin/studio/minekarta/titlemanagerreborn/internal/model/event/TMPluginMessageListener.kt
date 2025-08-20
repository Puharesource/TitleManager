package studio.minekarta.titlemanagerreborn.internal.model.event

import studio.minekarta.titlemanagerreborn.internal.onPluginDisable
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.messaging.PluginMessageListener

class TMPluginMessageListener(private val plugin: Plugin, val body: (PluginMessageReceivedItem) -> Unit) {
    private val listener: PluginMessageListener = PluginMessageListener { channel, player, message ->
        try {
            body(PluginMessageReceivedItem(channel, player, message))
        } catch (e: Exception) {
        }
    }

    init {
        plugin.server.messenger.registerIncomingPluginChannel(plugin, "BungeeCord", listener)

        onPluginDisable { invalidate() }
    }

    fun addTo(collection: MutableCollection<TMPluginMessageListener>) = collection.add(this)

    fun invalidate() = plugin.server.messenger.unregisterIncomingPluginChannel(plugin, "BungeeCord", listener)
}
