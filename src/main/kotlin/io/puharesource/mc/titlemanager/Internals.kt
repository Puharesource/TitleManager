package io.puharesource.mc.titlemanager

import io.puharesource.mc.titlemanager.config.TMConfigMain
import org.bukkit.Bukkit
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import rx.Scheduler
import rx.schedulers.Schedulers
import java.math.BigInteger
import java.util.Random
import java.util.concurrent.Executor

private val random = Random()

internal val pluginInstance : TitleManagerPlugin by lazy {
    Bukkit.getPluginManager().getPlugin("TitleManager") as TitleManagerPlugin
}

internal val pluginConfig : TMConfigMain by lazy {
    pluginInstance.tmConfig
}

internal val isTesting : Boolean
    get() = Bukkit.getServer() == null

internal val asyncExecutor = Executor { Bukkit.getScheduler().runTaskAsynchronously(pluginInstance, it) }
internal val syncExecutor = Executor { Bukkit.getScheduler().runTask(pluginInstance, it) }
internal val asyncScheduler : Scheduler = Schedulers.from { asyncExecutor }
internal val syncScheduler : Scheduler = Schedulers.from { syncExecutor }

internal fun info(message: String) = pluginInstance.logger.info(message)

internal fun warning(message: String) = pluginInstance.logger.warning(message)

internal fun debug(message: String) {
    if (pluginInstance.config.getBoolean("debug")) {
        info("[DEBUG] $message")
    }
}

internal fun onPluginDisable(body: () -> Unit) {
    val pluginManager = Bukkit.getPluginManager()
    val listener = object : Listener {}

    pluginManager.registerEvent(PluginDisableEvent::class.java, listener, EventPriority.MONITOR, { _, event ->
        val disableEvent = event as PluginDisableEvent

        if (disableEvent.plugin == pluginInstance) {
            body()
        }
    }, pluginInstance, false)
}

internal fun generateRandomString(): String = BigInteger(80, random).toString(32)