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
import net.kyori.adventure.title.Title
import java.time.Duration
import org.bukkit.scoreboard.Criteria
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import java.math.BigInteger
import java.util.Random
import java.util.UUID
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

    private val titleTimes = ConcurrentHashMap<UUID, BukkitApiTitleTicks>()

    override fun sendTitleTimes(player: Player, times: Title.Times) {
        titleTimes[player.uniqueId] = BukkitApiTitleTicks.from(times)
    }

    override fun sendTitle(player: Player, title: Component) {
        val times = titleTimes.current(player)
        player.sendTitle(title.toLegacyText(), "", times.fadeIn, times.stay, times.fadeOut)
    }

    override fun sendSubtitle(player: Player, subtitle: Component) {
        val times = titleTimes.current(player)
        player.sendTitle("", subtitle.toLegacyText(), times.fadeIn, times.stay, times.fadeOut)
    }

    override fun showTitle(player: Player, title: Title) {
        val times = BukkitApiTitleTicks.from(title.times() ?: DEFAULT_TITLE_TIMES)
        player.sendTitle(title.title().toLegacyText(), title.subtitle().toLegacyText(), times.fadeIn, times.stay, times.fadeOut)
    }

    override fun sendActionBar(player: Player, actionBar: Component) {
        player.sendActionBar(actionBar.toLegacyText())
    }

    override fun sendPlayerListHeaderAndFooter(player: Player, header: Component, footer: Component) {
        player.setPlayerListHeaderFooter(header.toLegacyText(), footer.toLegacyText())
    }

    override fun close() {
        titleTimes.clear()
    }

    private fun ConcurrentHashMap<UUID, BukkitApiTitleTicks>.current(player: Player): BukkitApiTitleTicks {
        return get(player.uniqueId) ?: DEFAULT_TITLE_TICKS
    }

    override fun createSidebar(player: Player): RuntimeSidebar {
        val scoreboard = server.scoreboardManager.newScoreboard

        player.scoreboard = scoreboard

        return ScoreboardRuntimeSidebar(server, player, BukkitApiScoreboardHandler(scoreboard))
    }

    companion object {
        private val DEFAULT_TITLE_TICKS = BukkitApiTitleTicks(10, 70, 20)
        private val DEFAULT_TITLE_TIMES = Title.Times.times(
            Duration.ofMillis(500),
            Duration.ofMillis(3500),
            Duration.ofMillis(1000)
        )

        fun isCompatible(serverVersion: RuntimeServerVersion): Boolean {
            val version = serverVersion.minecraftVersion ?: return true
            val parts = version.split('.').mapNotNull { it.toIntOrNull() }
            val major = parts.getOrNull(0) ?: return false
            val minor = parts.getOrNull(1) ?: return false

            return major > 1 || (major == 1 && minor >= 17)
        }
    }
}

private data class BukkitApiTitleTicks(
    val fadeIn: Int,
    val stay: Int,
    val fadeOut: Int
) {
    companion object {
        fun from(times: Title.Times): BukkitApiTitleTicks {
            return BukkitApiTitleTicks(
                fadeIn = times.fadeIn().toTicks(),
                stay = times.stay().toTicks(),
                fadeOut = times.fadeOut().toTicks()
            )
        }

        private fun Duration.toTicks(): Int {
            return toMillis()
                .coerceAtLeast(0)
                .div(50)
                .coerceAtMost(Int.MAX_VALUE.toLong())
                .toInt()
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
            objective.setDisplayName(value.take(128))
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
        getOrCreateTeam(index).setPrefix(text)
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

        return scoreboard.getObjective(name) ?: scoreboard.registerNewObjective(name, Criteria.DUMMY, title)
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

        private fun generateRandomString(): String = BigInteger(80, random).toString(32)
    }
}
