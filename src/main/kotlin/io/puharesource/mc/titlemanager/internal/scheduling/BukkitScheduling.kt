package io.puharesource.mc.titlemanager.internal.scheduling

import io.puharesource.mc.titlemanager.internal.onPluginDisable
import io.puharesource.mc.titlemanager.internal.pluginInstance
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import rx.Observable
import rx.subscriptions.Subscriptions
import java.util.concurrent.atomic.AtomicInteger

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

// rx / Observables

internal fun scheduleSyncObservableTimer(delay: Int = 0, period: Int = 1) : Observable<Int> {
    return Observable.create { subscriber ->
        val currentTicks = AtomicInteger(delay)

        val runnable = createBukkitRunnable {
            subscriber.onNext(currentTicks.get())

            currentTicks.addAndGet(period)
        }

        val task = runnable.runTaskTimer(pluginInstance, delay.toLong(), period.toLong())

        subscriber.add(Subscriptions.create {
            val scheduler = Bukkit.getScheduler()
            val taskId = task.taskId

            if (scheduler.isCurrentlyRunning(taskId) || scheduler.isQueued(taskId)) {
                scheduler.cancelTask(taskId)
                subscriber.onCompleted()
            }
        })

        onPluginDisable { subscriber.onCompleted() }
    }
}

internal fun scheduleAsyncObservableTimer(delay: Int = 0, period: Int = 1) : Observable<Int> {
    return Observable.create { subscriber ->
        val currentTicks = AtomicInteger(delay)

        val runnable = createBukkitRunnable {
            subscriber.onNext(currentTicks.get())

            currentTicks.addAndGet(period)
        }

        val task = runnable.runTaskTimerAsynchronously(pluginInstance, delay.toLong(), period.toLong())

        subscriber.add(Subscriptions.create {
            val scheduler = Bukkit.getScheduler()
            val taskId = task.taskId

            if (scheduler.isCurrentlyRunning(taskId) || scheduler.isQueued(taskId)) {
                scheduler.cancelTask(taskId)
                subscriber.onCompleted()
            }
        })

        onPluginDisable { subscriber.onCompleted() }
    }
}