package dev.tarkan.titlemanager.bukkit.listeners

import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.concurrency.ConcurrencyType
import dev.tarkan.titlemanager.bukkit.concurrency.CoroutineScopeManager
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import java.io.Closeable

abstract class TitleManagerListener(protected val plugin: TitleManagerPlugin, protected val coroutineScopeManager: CoroutineScopeManager) : Listener, Closeable {
    protected inline fun <reified TEvent : Event> registerEventExecutor(priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false, concurrencyType: ConcurrencyType = ConcurrencyType.UNDEFINED, crossinline body: suspend (TEvent) -> Unit) {
        val executor = createEventExecutor(concurrencyType, body)

        plugin.server.pluginManager.registerEvent(TEvent::class.java, this, priority, executor, plugin, ignoreCancelled)
    }

    protected inline fun <reified TEvent : Event> createEventExecutor(concurrencyType: ConcurrencyType = ConcurrencyType.UNDEFINED, crossinline body: suspend (TEvent) -> Unit) = EventExecutor { _, event ->
        if (event !is TEvent) {
            return@EventExecutor
        }

        coroutineScopeManager.run(concurrencyType) {
            body(event)
        }
    }

    override fun close() {
        HandlerList.unregisterAll(this)
    }
}