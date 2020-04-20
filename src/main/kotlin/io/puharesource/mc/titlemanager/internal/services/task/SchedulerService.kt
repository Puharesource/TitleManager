package io.puharesource.mc.titlemanager.internal.services.task

import java.util.concurrent.TimeUnit

interface SchedulerService {
    fun schedule(runnable: Runnable, delay: Int) = schedule({ runnable.run() }, delay)
    fun schedule(body: () -> Unit, delay: Int): Int
    fun schedule(runnable: Runnable, delay: Int, period: Int): Int = schedule({ runnable.run() }, delay, period)
    fun schedule(body: () -> Unit, delay: Int, period: Int): Int

    fun scheduleRaw(body: () -> Unit, delay: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): Int
    fun scheduleRaw(runnable: Runnable, delay: Int, period: Int): Int = scheduleRaw({ runnable.run() }, delay, period)
    fun scheduleRaw(body: () -> Unit, delay: Int, period: Int, unit: TimeUnit = TimeUnit.MILLISECONDS): Int

    fun cancel(taskId: Int)
    fun cancelAll()
}
