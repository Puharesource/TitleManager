package studio.minekarta.titlemanagerreborn.internal.services

import studio.minekarta.titlemanagerreborn.internal.config.TMConfigMain
import studio.minekarta.titlemanagerreborn.internal.services.animation.AnimationsService
import studio.minekarta.titlemanagerreborn.internal.services.animation.ScriptService
import studio.minekarta.titlemanagerreborn.internal.services.announcer.AnnouncerService
import studio.minekarta.titlemanagerreborn.internal.services.bungeecord.BungeeCordService
import studio.minekarta.titlemanagerreborn.internal.services.event.ListenerService
import studio.minekarta.titlemanagerreborn.internal.services.features.PlayerListService
import studio.minekarta.titlemanagerreborn.internal.services.features.ScoreboardService
import studio.minekarta.titlemanagerreborn.internal.services.placeholder.PlaceholderService
import studio.minekarta.titlemanagerreborn.internal.services.task.SchedulerService
import studio.minekarta.titlemanagerreborn.internal.services.task.TaskService
import org.bstats.bukkit.Metrics
import org.bstats.charts.SimplePie
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

        metrics.addCustomChart(SimplePie("script_engine") { scriptService.engineName })

        placeholderService.loadBuiltinPlaceholders()

        metrics.addCustomChart(SimplePie("servers_using_config") { config.usingConfig.toString() })

        if (config.usingConfig) {
            metrics.addCustomChart(SimplePie("servers_using_player_list") { config.playerList.enabled.toString() })
            metrics.addCustomChart(SimplePie("servers_using_scoreboard") { config.scoreboard.enabled.toString() })
            metrics.addCustomChart(SimplePie("servers_using_bungeecord_features") { config.usingBungeecord.toString() })
            metrics.addCustomChart(SimplePie("servers_using_announcer") { config.announcer.enabled.toString() })

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
