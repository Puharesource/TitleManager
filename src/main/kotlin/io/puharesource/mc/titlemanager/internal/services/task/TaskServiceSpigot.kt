package io.puharesource.mc.titlemanager.internal.services.task

import io.puharesource.mc.titlemanager.TitleManagerPlugin
import io.puharesource.mc.titlemanager.internal.config.TMConfigMain
import io.puharesource.mc.titlemanager.internal.debug
import io.puharesource.mc.titlemanager.internal.info
import io.puharesource.mc.titlemanager.internal.services.update.UpdateService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.io.IOException
import javax.inject.Inject

class TaskServiceSpigot @Inject constructor(private val plugin: TitleManagerPlugin, private val config: TMConfigMain, private val updateService: UpdateService) : TaskService {
    private val defaultTasks = mutableSetOf<BukkitTask>()

    override fun scheduleSync(delay: Long, body: () -> Unit): BukkitTask {
        if (delay <= 0) {
            return createBukkitRunnable(body).runTask(plugin)
        }

        return createBukkitRunnable(body).runTaskLater(plugin, delay)
    }

    override fun scheduleAsync(delay: Long, body: () -> Unit): BukkitTask {
        if (delay <= 0) {
            return createBukkitRunnable(body).runTaskAsynchronously(plugin)
        }

        return createBukkitRunnable(body).runTaskLaterAsynchronously(plugin, delay)
    }

    override fun scheduleSyncTimer(delay: Long, period: Long, body: () -> Unit): BukkitTask {
        return createBukkitRunnable(body).runTaskTimer(plugin, delay, period)
    }

    override fun scheduleAsyncTimer(delay: Long, period: Long, body: () -> Unit): BukkitTask {
        return createBukkitRunnable(body).runTaskTimerAsynchronously(plugin, delay, period)
    }

    override fun startDefaultTasks() {
        if (config.checkForUpdates) {
            startUpdaterTask()
        }
    }

    override fun stopDefaultTasks() {
        defaultTasks.forEach { it.cancel() }
    }

    private fun createBukkitRunnable(body: () -> Unit) = object : BukkitRunnable() {
        override fun run() {
            body()
        }
    }

    private fun startUpdaterTask() {
        val task = scheduleAsyncTimer(period = 20 * 60 * 10) {
            GlobalScope.launch {
                debug("Searching for updates...")

                try {
                    updateService.updateLatestVersionCache()
                } catch (e: IOException) {
                    debug("Failed to get information for update check.")
                }

                if (updateService.isUpdateAvailable) {
                    info("An update was found!")
                } else {
                    debug("No update was found.")
                }
            }
        }

        defaultTasks.add(task)
    }
}
