package io.puharesource.mc.titlemanager.internal.services.task

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class SchedulerServiceAsync : SchedulerService {
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private val scheduled: MutableMap<Int, ScheduledFuture<*>> = ConcurrentHashMap()
    private val ids = AtomicInteger(0)

    override fun schedule(body: () -> Unit, delay: Int): Int {
        val id = ids.incrementAndGet()

        scheduled[id] = scheduler.schedule({
            scheduled.remove(id)
            body()
        }, delay * 50L, TimeUnit.MILLISECONDS)

        return id
    }

    override fun schedule(body: () -> Unit, delay: Int, period: Int): Int {
        val id = ids.incrementAndGet()

        scheduled[id] = scheduler.scheduleAtFixedRate({ body() }, delay * 50L, period * 50L, TimeUnit.MILLISECONDS)

        return id
    }

    override fun scheduleRaw(body: () -> Unit, delay: Long, unit: TimeUnit): Int {
        val id = ids.incrementAndGet()

        scheduled[id] = scheduler.schedule({
            scheduled.remove(id)
            body()
        }, delay, unit)

        return id
    }

    override fun scheduleRaw(body: () -> Unit, delay: Int, period: Int, unit: TimeUnit): Int {
        val id = ids.incrementAndGet()

        scheduled[id] = scheduler.scheduleAtFixedRate({ body() }, delay.toLong(), period.toLong(), unit)

        return id
    }

    override fun cancel(taskId: Int) {
        scheduled[taskId]?.let {
            it.cancel(false)
            scheduled.remove(taskId)
        }
    }

    override fun cancelAll() {
        scheduled.values.forEach { it.cancel(false) }
        scheduled.clear()
    }
}
