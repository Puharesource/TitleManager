package io.puharesource.mc.titlemanager.internal.services.features

import io.puharesource.mc.titlemanager.TitleManagerPlugin
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import io.puharesource.mc.titlemanager.api.v2.animation.SendableAnimation
import io.puharesource.mc.titlemanager.internal.config.TMConfigMain
import io.puharesource.mc.titlemanager.internal.model.animation.EasySendableAnimation
import io.puharesource.mc.titlemanager.internal.model.animation.PartBasedSendableAnimation
import io.puharesource.mc.titlemanager.internal.model.scoreboard.ScoreboardHandler
import io.puharesource.mc.titlemanager.internal.services.animation.AnimationsService
import io.puharesource.mc.titlemanager.internal.services.placeholder.PlaceholderService
import io.puharesource.mc.titlemanager.internal.services.storage.PlayerInfoService
import io.puharesource.mc.titlemanager.internal.services.task.SchedulerService
import org.apache.commons.lang.RandomStringUtils
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scoreboard.Objective
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class ScoreboardServiceSpigot @Inject constructor(
    private val plugin: TitleManagerPlugin,
    private val config: TMConfigMain,
    private val placeholderService: PlaceholderService,
    private val schedulerService: SchedulerService,
    private val animationsService: AnimationsService,
    private val playerInfoService: PlayerInfoService
) : ScoreboardService {
    private val playerScoreboards: MutableMap<Player, ScoreboardHandler> = ConcurrentHashMap()
    private val playerScoreboardUpdateTasks: MutableMap<Player, Int> = ConcurrentHashMap()
    private val playerTeamCache: MutableMap<Player, Array<String>> = ConcurrentHashMap()

    override fun startPlayerTasks() {
        plugin.server.onlinePlayers.forEach {
            if (playerInfoService.isScoreboardEnabled(it)) {
                toggleScoreboardInWorld(it, it.world)
            }
        }
    }

    override fun stopPlayerTasks() {
        plugin.server.onlinePlayers.forEach {
            removeScoreboard(it)
        }
    }

    override fun hasScoreboard(player: Player): Boolean {
        val scoreboardRepresentation = playerScoreboards[player] ?: return false

        return player.scoreboard == scoreboardRepresentation.scoreboard
    }

    override fun giveScoreboard(player: Player) {
        if (!hasScoreboard(player)) {
            val scoreboard = Bukkit.getScoreboardManager().newScoreboard

            player.scoreboard = scoreboard
            playerScoreboards[player] = ScoreboardHandler(scoreboard)
            startUpdateTask(player)
        }
    }

    override fun giveDefaultScoreboard(player: Player) {
        if (!hasScoreboard(player)) {
            giveScoreboard(player)

            setProcessedScoreboardTitle(player, config.scoreboard.title)

            config.scoreboard.lines.forEachIndexed { index, line ->
                setProcessedScoreboardValue(player, index + 1, line)
            }
        }
    }

    override fun removeScoreboard(player: Player) {
        clearTeamCache(player)

        playerScoreboards.remove(player)?.let { scoreboardRepresentation ->
            stopUpdateTask(player)

            removeRunningScoreboardTitleAnimation(player)
            (1..15).forEach { removeRunningScoreboardValueAnimation(player, it) }

            scoreboardRepresentation.scoreboard.objectives.forEach(Objective::unregister)
        }
    }

    override fun getScoreboardTitle(player: Player): String? {
        return playerScoreboards[player]?.title
    }

    override fun setScoreboardTitle(player: Player, title: String, withPlaceholders: Boolean) {
        var processedTitle = title

        if (withPlaceholders) {
            processedTitle = placeholderService.replaceText(player, processedTitle)
        }

        playerScoreboards[player]?.title = processedTitle
    }

    override fun setProcessedScoreboardTitle(player: Player, title: String) {
        removeRunningScoreboardTitleAnimation(player)
        val parts = animationsService.textToAnimationParts(title)

        createScoreboardTitleSendableAnimation(parts, player, withPlaceholders = true).start()
    }

    override fun getScoreboardValue(player: Player, index: Int): String? {
        require(index in 1..15) { "Index needs to be in the range of 1 to 15 (1 and 15 inclusive). Index provided: $index" }

        return playerScoreboards[player]?.get(index)
    }

    override fun setScoreboardValue(player: Player, index: Int, value: String, withPlaceholders: Boolean) {
        require(index in 1..15) { "Index needs to be in the range of 1 to 15 (1 and 15 inclusive). Index provided: $index" }

        var processedValue = value

        if (withPlaceholders) {
            processedValue = placeholderService.replaceText(player, processedValue)
        }

        playerScoreboards[player]?.set(index, processedValue)
    }

    override fun setProcessedScoreboardValue(player: Player, index: Int, value: String) {
        require(index in 1..15) { "Index needs to be in the range of 1 to 15 (1 and 15 inclusive). Index provided: $index" }

        removeRunningScoreboardValueAnimation(player, index)

        val parts = animationsService.textToAnimationParts(value)

        createScoreboardValueSendableAnimation(parts, player, index, withPlaceholders = true).start()
    }

    override fun removeScoreboardValue(player: Player, index: Int) {
        require(index in 1..15) { "Index needs to be in the range of 1 to 15 (1 and 15 inclusive). Index provided: $index" }

        playerScoreboards[player]?.remove(index)
    }

    override fun createScoreboardTitleSendableAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(
            schedulerService,
            animation,
            player,
            {
                setScoreboardTitle(player, it.text, withPlaceholders = withPlaceholders)
            },
            isContinuous = true,
            tickRate = config.bandwidth.scoreboardMsPerTick,
            fixedOnStop = {
                removeRunningScoreboardTitleAnimation(player)
            },
            fixedOnStart = { receiver, sendableAnimation ->
                setRunningScoreboardTitleAnimation(receiver, sendableAnimation)
            }
        )
    }

    override fun createScoreboardValueSendableAnimation(animation: Animation, player: Player, index: Int, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(
            schedulerService,
            animation,
            player,
            {
                setScoreboardValue(player, index, it.text, withPlaceholders = withPlaceholders)
            },
            isContinuous = true,
            tickRate = config.bandwidth.scoreboardMsPerTick,
            fixedOnStop = {
                removeRunningScoreboardTitleAnimation(player)
            },
            fixedOnStart = { receiver, sendableAnimation ->
                setRunningScoreboardValueAnimation(receiver, index, sendableAnimation)
            }
        )
    }

    override fun createScoreboardTitleSendableAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(
            schedulerService,
            parts,
            player,
            {
                setScoreboardTitle(player, it.text, withPlaceholders = withPlaceholders)
            },
            isContinuous = true,
            tickRate = config.bandwidth.scoreboardMsPerTick,
            fixedOnStop = {
                removeRunningScoreboardTitleAnimation(player)
            },
            fixedOnStart = { receiver, animation ->
                setRunningScoreboardTitleAnimation(receiver, animation)
            }
        )
    }

    override fun createScoreboardValueSendableAnimation(parts: List<AnimationPart<*>>, player: Player, index: Int, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(
            schedulerService,
            parts,
            player,
            {
                setScoreboardValue(player, index, it.text, withPlaceholders = withPlaceholders)
            },
            isContinuous = true,
            tickRate = config.bandwidth.scoreboardMsPerTick,
            fixedOnStop = {
                removeRunningScoreboardTitleAnimation(player)
            },
            fixedOnStart = { receiver, animation ->
                setRunningScoreboardValueAnimation(receiver, index, animation)
            }
        )
    }

    override fun isScoreboardDisabledWorld(world: World) = config.scoreboard.disabledWorlds.any { disabledWorldName -> disabledWorldName.equals(world.name, ignoreCase = true) }

    override fun toggleScoreboardInWorld(player: Player, world: World) {
        if (!playerInfoService.isScoreboardEnabled(player)) {
            return
        }

        val isDisabledWorld = isScoreboardDisabledWorld(world)

        if (isDisabledWorld && hasScoreboard(player)) {
            removeScoreboard(player)
        } else if (!isDisabledWorld) {
            giveDefaultScoreboard(player)
        }
    }

    override fun populateTeamCache(player: Player) {
        val array = arrayOfNulls<String>(16)
        for (i in array.indices) {
            array[i] = RandomStringUtils.randomAlphanumeric(16)
        }

        playerTeamCache[player] = array as Array<String>
    }

    override fun clearTeamCache(player: Player) {
        playerTeamCache.remove(player)
    }

    override fun getTeamCache(player: Player): Array<String> {
        if (!playerTeamCache.containsKey(player)) {
            populateTeamCache(player)
        }

        return playerTeamCache[player]!!
    }

    private fun startUpdateTask(player: Player) {
        if (playerScoreboards.containsKey(player) && !playerScoreboardUpdateTasks.containsKey(player)) {
            playerScoreboardUpdateTasks[player] = schedulerService.schedule(
                {
                    val scoreboardHandler = playerScoreboards[player]
                    if (scoreboardHandler == null) {
                        stopUpdateTask(player)
                        return@schedule
                    }

                    scoreboardHandler.update()
                },
                1,
                1
            )
        }
    }

    private fun stopUpdateTask(player: Player) {
        playerScoreboardUpdateTasks.remove(player)?.let {
            schedulerService.cancel(it)
        }
    }

    private fun setRunningAnimation(player: Player, path: String, animation: SendableAnimation) {
        player.setMetadata("running-$path-animation", FixedMetadataValue(plugin, animation))
    }

    private fun removeRunningAnimation(player: Player, path: String) {
        val fullPath = "running-$path-animation"

        if (player.hasMetadata(fullPath)) {
            (player.getMetadata(fullPath).first().value() as? SendableAnimation)?.stop()

            player.removeMetadata(fullPath, plugin)
        }
    }

    private fun setRunningScoreboardTitleAnimation(player: Player, animation: SendableAnimation) = setRunningAnimation(player, "scoreboardtitle", animation)
    private fun removeRunningScoreboardTitleAnimation(player: Player) = removeRunningAnimation(player, "scoreboardtitle")

    private fun setRunningScoreboardValueAnimation(player: Player, index: Int, animation: SendableAnimation) = setRunningAnimation(player, "scoreboardvalue$index", animation)
    private fun removeRunningScoreboardValueAnimation(player: Player, index: Int) = removeRunningAnimation(player, "scoreboardvalue$index")
}
