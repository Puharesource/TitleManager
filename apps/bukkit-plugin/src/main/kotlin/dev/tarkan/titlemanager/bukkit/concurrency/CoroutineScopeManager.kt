package dev.tarkan.titlemanager.bukkit.concurrency

import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.configuration.AdvancedConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.Closeable
import java.util.concurrent.Executors

class CoroutineScopeManager(plugin: TitleManagerPlugin, advancedConfiguration: AdvancedConfiguration) : Closeable {
    private val syncJob = SupervisorJob()
    private val asyncJob = SupervisorJob()

    val syncContext = plugin.server.scheduler.getMainThreadExecutor(plugin).asCoroutineDispatcher()
    val context = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

    val syncScope = CoroutineScope(syncJob + syncContext)
    val scope = CoroutineScope(asyncJob + context)

    inline fun <T> run(concurrencyType: ConcurrencyType, crossinline body: suspend CoroutineScope.() -> T) {
        when (concurrencyType) {
            ConcurrencyType.UNDEFINED -> runBlocking {
                body()
            }
            ConcurrencyType.SYNC -> syncScope.launch {
                body()
            }
            ConcurrencyType.ASYNC -> scope.launch {
                body()
            }
        }
    }

    override fun close() {
        syncScope.cancel("TitleManager disabled")
        scope.cancel("TitleManager disabled")
        context.close()
    }
}