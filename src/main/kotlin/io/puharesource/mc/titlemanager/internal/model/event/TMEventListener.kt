package io.puharesource.mc.titlemanager.internal.model.event

import io.puharesource.mc.titlemanager.internal.onPluginDisable
import io.puharesource.mc.titlemanager.internal.services.task.TaskService
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.Plugin

class TMEventListener<T : Event>(plugin: Plugin, taskService: TaskService? = null, priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false, vararg events: Class<T>, body: (T) -> Unit) {
    private val listener: Listener = object : Listener {}
    private var delay: Long? = null

    init {
        val executor = EventExecutor { _, event ->
            val eventClass = event.javaClass
            val canAssign = events.any { it.isAssignableFrom(eventClass) }

            if (!canAssign) return@EventExecutor

            if (taskService != null) {
                if (delay != null) {
                    taskService.scheduleAsync(delay!!) { body(event as T) }
                } else {
                    taskService.scheduleAsync { body(event as T) }
                }
            } else {
                body(event as T)
            }
        }

        val pluginManager = Bukkit.getPluginManager()

        events.forEach { pluginManager.registerEvent(it, listener, priority, executor, plugin, ignoreCancelled) }

        onPluginDisable { invalidate() }
    }

    fun delay(delay: Long): TMEventListener<T> {
        this.delay = delay

        return this
    }

    fun addTo(collection: MutableCollection<TMEventListener<*>>) = collection.add(this)

    fun invalidate() = HandlerList.unregisterAll(listener)
}
