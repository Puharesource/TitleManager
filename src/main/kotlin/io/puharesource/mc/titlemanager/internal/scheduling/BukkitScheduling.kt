package io.puharesource.mc.titlemanager.internal.scheduling

import io.puharesource.mc.titlemanager.internal.pluginInstance
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

// Standard

private fun createBukkitRunnable(body: () -> Unit) = object : BukkitRunnable() {
    override fun run() { body() }
}

internal fun scheduleSync(delay: Long = 0, body: () -> Unit) : BukkitTask {
    if (delay <= 0) {
        return createBukkitRunnable(body).runTask(pluginInstance)
    }

    return createBukkitRunnable(body).runTaskLater(pluginInstance, delay)
}

internal fun scheduleAsync(delay: Long = 0, body: () -> Unit) : BukkitTask {
    if (delay <= 0) {
        return createBukkitRunnable(body).runTaskAsynchronously(pluginInstance)
    }

    return createBukkitRunnable(body).runTaskLaterAsynchronously(pluginInstance, delay)
}

internal fun scheduleSyncTimer(delay: Long = 0, period: Long = 1, body: () -> Unit) : BukkitTask {
    return createBukkitRunnable(body).runTaskTimer(pluginInstance, delay, period)
}

internal fun scheduleAsyncTimer(delay: Long = 0, period: Long = 1, body: () -> Unit) : BukkitTask {
    return createBukkitRunnable(body).runTaskTimerAsynchronously(pluginInstance, delay, period)
}
