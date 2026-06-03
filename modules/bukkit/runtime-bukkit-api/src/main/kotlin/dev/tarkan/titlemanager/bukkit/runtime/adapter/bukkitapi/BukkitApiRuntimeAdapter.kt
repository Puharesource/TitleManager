package dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi

import dev.tarkan.titlemanager.bukkit.diagnostics.DiagnosticsStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapability
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapabilityStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeServerVersion
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeSidebar
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeThreadingPolicy
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeVersionModule
import dev.tarkan.titlemanager.bukkit.runtime.RuntimeCapabilityDetail
import dev.tarkan.titlemanager.bukkit.runtime.RuntimeTextConstants
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import org.bukkit.scoreboard.Criteria
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import java.math.BigInteger
import java.util.Random
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class BukkitApiRuntimeAdapter(
    private val server: Server,
    private val serverVersion: RuntimeServerVersion = RuntimeServerVersion.from(server)
) : RuntimeVersionModule {
    override val id = "bukkit-api"
    override val displayName = "$id (${serverVersion.displayVersion})"
    override val threadingPolicy = RuntimeThreadingPolicy.mainThreadOnly()
    override val capabilities = listOf(
        DiagnosticsStatus(RuntimeCapability.TITLES, RuntimeCapabilityStatus.AVAILABLE, RuntimeCapabilityDetail.ADVENTURE_API),
        DiagnosticsStatus(RuntimeCapability.ACTIONBAR, RuntimeCapabilityStatus.AVAILABLE, RuntimeCapabilityDetail.ADVENTURE_API),
        DiagnosticsStatus(RuntimeCapability.PLAYER_LIST, RuntimeCapabilityStatus.AVAILABLE, RuntimeCapabilityDetail.ADVENTURE_API),
        DiagnosticsStatus(RuntimeCapability.SIDEBAR, RuntimeCapabilityStatus.AVAILABLE, RuntimeCapabilityDetail.BUKKIT_SCOREBOARD_API),
        DiagnosticsStatus(RuntimeCapability.DIRECT_NMS, RuntimeCapabilityStatus.UNAVAILABLE, "no direct module selected for ${serverVersion.displayVersion}")
    )

    override fun sendTitleTimes(player: Player, times: Title.Times) {
        player.sendTitlePart(TitlePart.TIMES, times)
    }

    override fun sendTitle(player: Player, title: Component) {
        player.sendTitlePart(TitlePart.TITLE, title)
    }

    override fun sendSubtitle(player: Player, subtitle: Component) {
        player.sendTitlePart(TitlePart.SUBTITLE, subtitle)
    }

    override fun showTitle(player: Player, title: Title) {
        player.showTitle(title)
    }

    override fun sendActionBar(player: Player, actionBar: Component) {
        player.sendActionBar(actionBar)
    }

    override fun sendPlayerListHeaderAndFooter(player: Player, header: Component, footer: Component) {
        player.sendPlayerListHeaderAndFooter(header, footer)
    }

    override fun createSidebar(player: Player): RuntimeSidebar {
        val scoreboard = server.scoreboardManager.newScoreboard

        player.scoreboard = scoreboard

        return ScoreboardRuntimeSidebar(server, player, BukkitApiScoreboardHandler(scoreboard))
    }

    companion object {
        fun isCompatible(serverVersion: RuntimeServerVersion): Boolean {
            val version = serverVersion.minecraftVersion ?: return true
            val parts = version.split('.').mapNotNull { it.toIntOrNull() }
            val major = parts.getOrNull(0) ?: return false
            val minor = parts.getOrNull(1) ?: return false

            return major > 1 || (major == 1 && minor >= 17)
        }
    }
}


private class BukkitApiScoreboardHandler(
    override val scoreboard: Scoreboard,
    title: String = RuntimeTextConstants.SCOREBOARD_DEFAULT_TITLE,
    private val lines: MutableMap<Int, String> = ConcurrentHashMap()
) : ScoreboardRuntimeSidebarHandler {
    private var objective: Objective
    private val isUpdatePending = AtomicBoolean(false)

    override var title: String = ""
        set(value) {
            if (field == value) {
                return
            }

            field = value
            objective.displayName(componentSerializer.deserialize(value))
            isUpdatePending.set(true)
        }

    init {
        this.objective = getOrCreateObjective(title.take(128))
        this.title = title
        this.objective.displaySlot = DisplaySlot.SIDEBAR
    }

    override fun get(index: Int) = lines[index]

    override fun set(index: Int, text: String) {
        val isNew = lines[index] != text

        lines[index] = text
        getOrCreateTeam(index).prefix(componentSerializer.deserialize(text))
        objective.getScore(getTeamName(index)).score = RuntimeTextConstants.sidebarScore(index)

        if (isNew) {
            isUpdatePending.set(true)
        }
    }

    override fun remove(index: Int) {
        val existed = lines.remove(index) != null

        if (existed) {
            val teamName = getTeamName(index)
            scoreboard.resetScores(teamName)
            scoreboard.getTeam(teamName)?.unregister()
            isUpdatePending.set(true)
        }
    }

    private fun getTeamName(index: Int): String = RuntimeTextConstants.scoreboardEntry(index)

    private fun getOrCreateObjective(title: String = this.title): Objective {
        val name = generateRandomString()

        return scoreboard.getObjective(name) ?: scoreboard.registerNewObjective(name, Criteria.DUMMY, componentSerializer.deserialize(title))
    }

    private fun getOrCreateTeam(index: Int): Team {
        val name = getTeamName(index)
        var team = scoreboard.getTeam(name)

        if (team != null) {
            return team
        }

        team = scoreboard.registerNewTeam(name)
        team.addEntry(name)

        return team
    }

    private companion object {
        private val random = Random()
        private val componentSerializer = LegacyComponentSerializer.legacy('§')

        private fun generateRandomString(): String = BigInteger(80, random).toString(32)
    }
}
