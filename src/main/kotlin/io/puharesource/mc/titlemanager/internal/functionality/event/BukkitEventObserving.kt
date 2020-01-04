package io.puharesource.mc.titlemanager.internal.functionality.event

import org.bukkit.event.Event
import org.bukkit.event.EventPriority

internal inline fun <reified T : Event> listenEventSync(priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false, crossinline body: (T) -> Unit) : TMEventListener<T> {
    return listenEventSync(priority, ignoreCancelled, T::class.java, body = { body(it) })
}

internal inline fun <reified T : Event> listenEventAsync(priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false, crossinline body: (T) -> Unit) : TMEventListener<T> {
    return listenEventAsync(priority, ignoreCancelled, T::class.java, body = { body(it) })
}

internal fun <T : Event> listenEventSync(priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false, vararg events: Class<T>, body: (T) -> Unit) = TMEventListener(false, priority, ignoreCancelled, *events, body = body)

internal fun <T : Event> listenEventAsync(priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false, vararg events: Class<T>, body: (T) -> Unit) = TMEventListener(true, priority, ignoreCancelled, *events, body = body)

internal fun observePluginMessageReceived(body: (PluginMessageReceivedItem) -> Unit) = TMPluginMessageListener(body)