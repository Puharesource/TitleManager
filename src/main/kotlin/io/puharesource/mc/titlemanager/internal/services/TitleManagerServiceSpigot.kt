package io.puharesource.mc.titlemanager.internal.services

import io.puharesource.mc.titlemanager.internal.config.TMConfigMain
import io.puharesource.mc.titlemanager.internal.services.animation.AnimationsService
import io.puharesource.mc.titlemanager.internal.services.animation.ScriptService
import io.puharesource.mc.titlemanager.internal.services.announcer.AnnouncerService
import io.puharesource.mc.titlemanager.internal.services.bungeecord.BungeeCordService
import io.puharesource.mc.titlemanager.internal.services.event.ListenerService
import io.puharesource.mc.titlemanager.internal.services.features.PlayerListService
import io.puharesource.mc.titlemanager.internal.services.features.ScoreboardService
import io.puharesource.mc.titlemanager.internal.services.placeholder.PlaceholderService
import io.puharesource.mc.titlemanager.internal.services.task.SchedulerService
import io.puharesource.mc.titlemanager.internal.services.task.TaskService
import org.bstats.bukkit.Metrics
import javax.inject.Inject

class TitleManagerServiceSpigot @Inject constructor(
    private val config: TMConfigMain,
    private val listenerService: ListenerService,
    private val taskService: TaskService,
    private val schedulerService: SchedulerService,
    private val animationsService: AnimationsService,
    private val scriptService: ScriptService,
    private val placeholderService: PlaceholderService,
    private val playerListService: PlayerListService,
    private val scoreboardService: ScoreboardService,
    private val bungeeCordService: BungeeCordService,
    private val announcerService: AnnouncerService,
    private val metrics: Metrics
) : TitleManagerService {
    override fun start() {
        listenerService.registerListeners()
        taskService.startDefaultTasks()

        animationsService.createAnimationsFolderIfNotExists()
        animationsService.loadAnimations()

        scriptService.loadScripts()

        metrics.addCustomChart(Metrics.SimplePie("script_engine") { scriptService.engineName })

        placeholderService.loadBuiltinPlaceholders()

        metrics.addCustomChart(Metrics.SimplePie("servers_using_config") { config.usingConfig.toString() })

        if (config.usingConfig) {
            metrics.addCustomChart(Metrics.SimplePie("servers_using_player_list") { config.playerList.enabled.toString() })
            metrics.addCustomChart(Metrics.SimplePie("servers_using_scoreboard") { config.scoreboard.enabled.toString() })
            metrics.addCustomChart(Metrics.SimplePie("servers_using_bungeecord_features") { config.usingBungeecord.toString() })
            metrics.addCustomChart(Metrics.SimplePie("servers_using_announcer") { config.announcer.enabled.toString() })

            if (config.playerList.enabled) {
                playerListService.startPlayerTasks()
            }

            if (config.scoreboard.enabled) {
                scoreboardService.startPlayerTasks()
            }

            if (config.usingBungeecord) {
                bungeeCordService.start()
            }

            if (config.announcer.enabled) {
                announcerService.start()
            }
        }
    }

    override fun stop() {
        listenerService.unregisterListeners()
        taskService.stopDefaultTasks()

        schedulerService.cancelAll()

        bungeeCordService.stop()
        announcerService.stop()
        scoreboardService.stopPlayerTasks()
    }
}
