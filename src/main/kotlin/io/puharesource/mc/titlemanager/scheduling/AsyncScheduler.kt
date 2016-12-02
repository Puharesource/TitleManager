package io.puharesource.mc.titlemanager.scheduling

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object AsyncScheduler {
    private val scheduler : ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private val scheduled : MutableMap<Int, ScheduledFuture<*>> = ConcurrentHashMap()
    private val ids = AtomicInteger(0)

    fun schedule(runnable: Runnable, delay: Int) = schedule({ runnable.run() }, delay)

    fun schedule(body: () -> Unit, delay: Int) : Int {
        val id = ids.incrementAndGet()

        scheduled.put(id, scheduler.schedule({
            scheduled.remove(id)
            body()
        }, delay * 50L, TimeUnit.MILLISECONDS))

        return id
    }

    fun schedule(runnable: Runnable, delay: Int, period: Int) : Int = schedule({ runnable.run() }, delay, period)

    fun schedule(body: () -> Unit, delay: Int, period: Int) : Int {
        val id = ids.incrementAndGet()

        scheduled.put(id, scheduler.scheduleAtFixedRate({ body() }, delay * 50L, period * 50L, TimeUnit.MILLISECONDS))

        return id
    }

    fun scheduleRaw(runnable: Runnable, delay: Int, period: Int) : Int = scheduleRaw({ runnable.run() }, delay, period)

    fun scheduleRaw(body: () -> Unit, delay: Int, period: Int, unit: TimeUnit = TimeUnit.MILLISECONDS) : Int {
        val id = ids.incrementAndGet()

        scheduled.put(id, scheduler.scheduleAtFixedRate({ body() }, delay * 50L, period * 50L, unit))

        return id
    }

    fun cancel(taskId: Int) {
        scheduled[taskId]?.let {
            it.cancel(false)
            scheduled.remove(taskId)
        }
    }

    fun cancelAll() {
        scheduled.values.forEach { it.cancel(false) }
        scheduled.clear()
    }
}