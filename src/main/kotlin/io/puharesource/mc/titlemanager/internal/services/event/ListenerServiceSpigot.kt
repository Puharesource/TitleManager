package io.puharesource.mc.titlemanager.internal.services.event

import io.puharesource.mc.titlemanager.TitleManagerPlugin
import io.puharesource.mc.titlemanager.internal.config.TMConfigMain
import io.puharesource.mc.titlemanager.internal.model.event.TMEventListener
import io.puharesource.mc.titlemanager.internal.services.features.ActionbarService
import io.puharesource.mc.titlemanager.internal.services.features.PlayerListService
import io.puharesource.mc.titlemanager.internal.services.features.ScoreboardService
import io.puharesource.mc.titlemanager.internal.services.features.TitleService
import io.puharesource.mc.titlemanager.internal.services.storage.PlayerInfoService
import io.puharesource.mc.titlemanager.internal.services.task.TaskService
import io.puharesource.mc.titlemanager.internal.services.update.UpdateService
import org.bukkit.ChatColor
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import javax.inject.Inject

class ListenerServiceSpigot @Inject constructor(
    private val config: TMConfigMain,
    private val plugin: TitleManagerPlugin,
    private val taskService: TaskService,
    private val updateService: UpdateService,
    private val playerInfoService: PlayerInfoService,
    private val titleService: TitleService,
    private val actionbarService: ActionbarService,
    private val playerListService: PlayerListService,
    private val scoreboardService: ScoreboardService
) : ListenerService {
    private val listeners: MutableSet<TMEventListener<*>> = mutableSetOf()

    override fun registerListeners() {
        if (config.checkForUpdates) {
            registerOnJoinUpdate()
        }

        registerOnQuitPlayerListCacheCleanup()
        registerOnQuitPlayerScoreboardCacheCleanup()

        if (config.usingConfig) {
            if (config.welcomeTitle.enabled) {
                registerOnWelcomeMessage()
            }

            if (config.welcomeActionbar.enabled) {
                registerOnWelcomeActionbarMessage()
            }

            if (config.playerList.enabled) {
                registerSetHeaderAndFooter()
            }

            if (config.scoreboard.enabled) {
                registerSetScoreboard()
            }
        }
    }

    override fun unregisterListeners() {
        this.listeners.forEach { it.invalidate() }
        this.listeners.clear()
    }

    private inline fun <reified T : Event> listenEventSync(priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false, crossinline body: (T) -> Unit): TMEventListener<T> {
        return listenEventSync(priority, ignoreCancelled, T::class.java, body = { body(it) })
    }

    private inline fun <reified T : Event> listenEventAsync(priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false, crossinline body: (T) -> Unit): TMEventListener<T> {
        return listenEventAsync(priority, ignoreCancelled, T::class.java, body = { body(it) })
    }

    private fun <T : Event> listenEventSync(priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false, vararg events: Class<T>, body: (T) -> Unit) = TMEventListener(plugin, null, priority, ignoreCancelled, *events, body = body)

    private fun <T : Event> listenEventAsync(priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false, vararg events: Class<T>, body: (T) -> Unit) = TMEventListener(plugin, taskService, priority, ignoreCancelled, *events, body = body)

    private fun registerOnJoinUpdate() {
        listenEventSync<PlayerJoinEvent> {
            if (!updateService.isUpdateAvailable) return@listenEventSync

            val player = it.player

            if (!player.hasPermission("titlemanager.update.notify")) return@listenEventSync

            player.sendMessage("${ChatColor.WHITE}[${ChatColor.GOLD}TitleManager${ChatColor.WHITE}] ${ChatColor.YELLOW}An update was found!")
            player.sendMessage("${ChatColor.YELLOW}You're currently on version ${updateService.currentVersion} while ${updateService.latestVersion} is available.")
            player.sendMessage("${ChatColor.YELLOW}Download it here:${ChatColor.GOLD}${ChatColor.UNDERLINE} http://www.spigotmc.org/resources/titlemanager.1049")
        }.addTo(listeners)
    }

    private fun registerOnQuitPlayerListCacheCleanup() {
        listenEventSync<PlayerQuitEvent> { playerListService.clearHeaderAndFooterCache(it.player) }.addTo(listeners)
    }

    private fun registerOnQuitPlayerScoreboardCacheCleanup() {
        listenEventSync<PlayerQuitEvent> {
            val player = it.player

            if (scoreboardService.hasScoreboard(player)) {
                scoreboardService.removeScoreboard(player)
            }
        }.addTo(listeners)
    }

    private fun registerOnWelcomeMessage() {
        listenEventAsync<PlayerJoinEvent> {
            val player = it.player

            if (!player.isOnline) return@listenEventAsync

            val welcomeTitle = config.welcomeTitle

            if (player.hasPlayedBefore()) {
                titleService.sendTitles(player, welcomeTitle.title, welcomeTitle.subtitle, welcomeTitle.fadeIn, welcomeTitle.stay, welcomeTitle.fadeOut, withPlaceholders = true)
            } else {
                titleService.sendTitles(player, welcomeTitle.firstJoin.title, welcomeTitle.firstJoin.subtitle, welcomeTitle.fadeIn, welcomeTitle.stay, welcomeTitle.fadeOut, withPlaceholders = true)
            }
        }.delay(20).addTo(listeners)
    }

    private fun registerOnWelcomeActionbarMessage() {
        listenEventAsync<PlayerJoinEvent> {
            val player = it.player

            if (!player.isOnline) return@listenEventAsync

            if (player.hasPlayedBefore()) {
                actionbarService.sendProcessedActionbar(player, config.welcomeActionbar.title)
            } else {
                actionbarService.sendProcessedActionbar(player, config.welcomeActionbar.firstJoin)
            }
        }.delay(20).addTo(listeners)
    }

    private fun registerSetHeaderAndFooter() {
        listenEventSync<PlayerJoinEvent> {
            val player = it.player

            playerListService.setProcessedHeaderAndFooter(player, config.playerList.header, config.playerList.footer)
        }.addTo(listeners)
    }

    private fun registerSetScoreboard() {
        listenEventSync<PlayerJoinEvent> {
            val player = it.player

            if (!playerInfoService.isScoreboardEnabled(player)) return@listenEventSync

            val title = config.scoreboard.title
            val lines = config.scoreboard.lines

            scoreboardService.giveScoreboard(player)
            scoreboardService.setProcessedScoreboardTitle(player, title)

            lines.forEachIndexed { index, text ->
                scoreboardService.setProcessedScoreboardValue(player, index + 1, text)
            }
        }.addTo(listeners)
    }
}
