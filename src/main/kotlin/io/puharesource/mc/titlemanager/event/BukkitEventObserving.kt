package io.puharesource.mc.titlemanager.event

import io.puharesource.mc.titlemanager.internal.onPluginDisable
import io.puharesource.mc.titlemanager.internal.pluginInstance
import io.puharesource.mc.titlemanager.internal.syncScheduler
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.messaging.PluginMessageListener
import rx.Observable
import rx.subscriptions.Subscriptions

internal inline fun <reified T : Event> observeEvent(priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false) : Observable<T> {
    return observeEventRaw(priority, ignoreCancelled, T::class.java).subscribeOn(syncScheduler)
}

internal inline fun <reified T : Event> observeEventRaw(priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false) : Observable<T> {
    return observeEventRaw(priority, ignoreCancelled, T::class.java)
}

internal fun <T : Event> observeEvent(priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false, vararg events: Class<T>) : Observable<T> {
    return observeEventRaw(priority, ignoreCancelled, *events).subscribeOn(syncScheduler)
}

internal fun <T : Event> observeEventRaw(priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false, vararg events: Class<T>) : Observable<T> {
    return Observable.create { subscriber ->
        val listener = object : Listener {}

        val executor = EventExecutor { _, event ->
            val eventClass = event.javaClass
            val canAssign = events.any { it.isAssignableFrom(eventClass) }

            if (!canAssign) return@EventExecutor

            subscriber.onNext(event as T)
        }

        val pluginManager = Bukkit.getPluginManager()

        events.forEach { pluginManager.registerEvent(it, listener, priority, executor, pluginInstance, ignoreCancelled) }

        subscriber.add(Subscriptions.create { HandlerList.unregisterAll(listener) })

        onPluginDisable { subscriber.onCompleted() }
    }
}

internal class PluginMessageReceivedItem(var channel: String, var player: Player, var message: ByteArray)

internal fun observePluginMessageReceived() : Observable<PluginMessageReceivedItem> {
    return Observable.create { subscriber ->
        val listener = PluginMessageListener { channel, player, message ->
            try {
                subscriber.onNext(PluginMessageReceivedItem(channel, player, message))
            } catch (e: Exception) {}
        }

        pluginInstance.server.messenger.registerIncomingPluginChannel(pluginInstance, "BungeeCord", listener)

        subscriber.add(Subscriptions.create { pluginInstance.server.messenger.unregisterIncomingPluginChannel(pluginInstance, "BungeeCord", listener) })

        onPluginDisable { subscriber.onCompleted() }
    }
}