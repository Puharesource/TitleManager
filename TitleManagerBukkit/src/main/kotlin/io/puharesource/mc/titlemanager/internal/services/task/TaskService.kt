package io.puharesource.mc.titlemanager.internal.services.task

import org.bukkit.scheduler.BukkitTask

interface TaskService {
    fun scheduleSync(delay: Long = 0, body: () -> Unit): BukkitTask
    fun scheduleAsync(delay: Long = 0, body: () -> Unit): BukkitTask

    fun scheduleSyncTimer(delay: Long = 0, period: Long = 1, body: () -> Unit): BukkitTask
    fun scheduleAsyncTimer(delay: Long = 0, period: Long = 1, body: () -> Unit): BukkitTask

    fun startDefaultTasks()
    fun stopDefaultTasks()
}
