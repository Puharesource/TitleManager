package io.puharesource.mc.titlemanager.internal.model.event

import io.puharesource.mc.titlemanager.internal.onPluginDisable
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.messaging.PluginMessageListener

class TMPluginMessageListener(private val plugin: Plugin, val body: (PluginMessageReceivedItem) -> Unit) {
    private val listener: PluginMessageListener

    init {
        listener = PluginMessageListener { channel, player, message ->
            try {
                body(PluginMessageReceivedItem(channel, player, message))
            } catch (e: Exception) {
            }
        }

        plugin.server.messenger.registerIncomingPluginChannel(plugin, "BungeeCord", listener)

        onPluginDisable { invalidate() }
    }

    fun addTo(collection: MutableCollection<TMPluginMessageListener>) = collection.add(this)

    fun invalidate() = plugin.server.messenger.unregisterIncomingPluginChannel(plugin, "BungeeCord", listener)
}
