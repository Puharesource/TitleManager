@file:Suppress("DEPRECATION")

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
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import java.math.BigInteger
import java.time.Duration
import java.util.Random
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class LegacySpigotRuntimeAdapter(
    private val server: Server,
    private val serverVersion: RuntimeServerVersion
) : RuntimeVersionModule {
    override val id = "legacy-spigot-api"
    override val displayName = "$id (${serverVersion.displayVersion})"
    override val threadingPolicy = RuntimeThreadingPolicy.mainThreadOnly()
    override val capabilities = listOf(
        DiagnosticsStatus(RuntimeCapability.TITLES, RuntimeCapabilityStatus.AVAILABLE, RuntimeCapabilityDetail.SPIGOT_TITLE_API),
        DiagnosticsStatus(RuntimeCapability.ACTIONBAR, RuntimeCapabilityStatus.AVAILABLE, RuntimeCapabilityDetail.SPIGOT_CHAT_MESSAGE_TYPE_API),
        DiagnosticsStatus(
            RuntimeCapability.PLAYER_LIST,
            RuntimeCapabilityStatus.UNAVAILABLE,
            RuntimeCapabilityDetail.LEGACY_PLAYER_LIST_REQUIRES_NMS
        ),
        DiagnosticsStatus(RuntimeCapability.SIDEBAR, RuntimeCapabilityStatus.AVAILABLE, RuntimeCapabilityDetail.BUKKIT_SCOREBOARD_STRING_API),
        DiagnosticsStatus(
            RuntimeCapability.DIRECT_NMS,
            RuntimeCapabilityStatus.UNAVAILABLE,
            "legacy Spigot API fallback selected; direct NMS module not available for ${serverVersion.displayVersion}"
        )
    )

    private val titleTimes = ConcurrentHashMap<UUID, LegacyTitleTicks>()

    override fun sendTitleTimes(player: Player, times: Title.Times) {
        titleTimes[player.uniqueId] = LegacyTitleTicks.from(times)
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
        val times = LegacyTitleTicks.from(title.times() ?: DEFAULT_TITLE_TIMES)
        player.sendTitle(title.title().toLegacyText(), title.subtitle().toLegacyText(), times.fadeIn, times.stay, times.fadeOut)
    }

    override fun sendActionBar(player: Player, actionBar: Component) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(actionBar.toLegacyText()))
    }

    override fun sendPlayerListHeaderAndFooter(player: Player, header: Component, footer: Component) {
        throw UnsupportedOperationException("${RuntimeCapability.PLAYER_LIST} is unavailable in the legacy Spigot API runtime")
    }

    override fun createSidebar(player: Player): RuntimeSidebar {
        val scoreboard = server.scoreboardManager.newScoreboard

        player.scoreboard = scoreboard

        return ScoreboardRuntimeSidebar(server, player, LegacySpigotScoreboardHandler(scoreboard))
    }

    override fun close() {
        titleTimes.clear()
    }

    private fun ConcurrentHashMap<UUID, LegacyTitleTicks>.current(player: Player): LegacyTitleTicks {
        return get(player.uniqueId) ?: DEFAULT_TITLE_TICKS
    }

    companion object {
        fun isCompatible(serverVersion: RuntimeServerVersion): Boolean {
            val version = serverVersion.minecraftVersion ?: return false
            val parts = version.split('.').mapNotNull { it.toIntOrNull() }
            val major = parts.getOrNull(0) ?: return false
            val minor = parts.getOrNull(1) ?: return false

            return major == 1 && minor in 12..16
        }

        private val DEFAULT_TITLE_TICKS = LegacyTitleTicks(10, 70, 20)
        private val DEFAULT_TITLE_TIMES = Title.Times.times(
            Duration.ofMillis(500),
            Duration.ofMillis(3500),
            Duration.ofMillis(1000)
        )
    }
}

private val legacyComponentSerializer = LegacyComponentSerializer.legacySection()

internal fun Component.toLegacyText(): String {
    return legacyComponentSerializer.serialize(this)
}

class LegacySpigotTitleOnlyRuntimeAdapter(
    private val server: Server,
    private val serverVersion: RuntimeServerVersion
) : RuntimeVersionModule {
    override val id = "legacy-spigot-title-api"
    override val displayName = "$id (${serverVersion.displayVersion})"
    override val threadingPolicy = RuntimeThreadingPolicy.mainThreadOnly()
    override val capabilities = listOf(
        DiagnosticsStatus(RuntimeCapability.TITLES, RuntimeCapabilityStatus.AVAILABLE, RuntimeCapabilityDetail.SPIGOT_TITLE_API_WITHOUT_TIMING),
        DiagnosticsStatus(
            RuntimeCapability.ACTIONBAR,
            RuntimeCapabilityStatus.UNAVAILABLE,
            RuntimeCapabilityDetail.LEGACY_ACTIONBAR_REQUIRES_NMS
        ),
        DiagnosticsStatus(
            RuntimeCapability.PLAYER_LIST,
            RuntimeCapabilityStatus.UNAVAILABLE,
            RuntimeCapabilityDetail.LEGACY_PLAYER_LIST_REQUIRES_NMS
        ),
        DiagnosticsStatus(RuntimeCapability.SIDEBAR, RuntimeCapabilityStatus.AVAILABLE, RuntimeCapabilityDetail.BUKKIT_SCOREBOARD_STRING_API),
        DiagnosticsStatus(
            RuntimeCapability.DIRECT_NMS,
            RuntimeCapabilityStatus.UNAVAILABLE,
            "legacy Spigot title fallback selected; direct NMS module not available for ${serverVersion.displayVersion}"
        )
    )

    override fun sendTitleTimes(player: Player, times: Title.Times) {
    }

    override fun sendTitle(player: Player, title: Component) {
        player.sendTitle(title.toLegacyText(), "")
    }

    override fun sendSubtitle(player: Player, subtitle: Component) {
        player.sendTitle("", subtitle.toLegacyText())
    }

    override fun showTitle(player: Player, title: Title) {
        player.sendTitle(title.title().toLegacyText(), title.subtitle().toLegacyText())
    }

    override fun sendActionBar(player: Player, actionBar: Component) {
        throw UnsupportedOperationException("${RuntimeCapability.ACTIONBAR} is unavailable in the title-only legacy Spigot API runtime")
    }

    override fun sendPlayerListHeaderAndFooter(player: Player, header: Component, footer: Component) {
        throw UnsupportedOperationException("${RuntimeCapability.PLAYER_LIST} is unavailable in the title-only legacy Spigot API runtime")
    }

    override fun createSidebar(player: Player): RuntimeSidebar {
        val scoreboard = server.scoreboardManager.newScoreboard

        player.scoreboard = scoreboard

        return ScoreboardRuntimeSidebar(server, player, LegacySpigotScoreboardHandler(scoreboard))
    }

    companion object {
        fun isCompatible(serverVersion: RuntimeServerVersion): Boolean {
            val version = serverVersion.minecraftVersion ?: return false
            val parts = version.split('.').mapNotNull { it.toIntOrNull() }
            val major = parts.getOrNull(0) ?: return false
            val minor = parts.getOrNull(1) ?: return false

            return major == 1 && minor in 8..11
        }
    }
}


private class LegacySpigotScoreboardHandler(
    override val scoreboard: Scoreboard,
    title: String = RuntimeTextConstants.SCOREBOARD_DEFAULT_TITLE,
    private val lines: MutableMap<Int, String> = ConcurrentHashMap()
) : ScoreboardRuntimeSidebarHandler {
    private var objective: Objective

    override var title: String = ""
        set(value) {
            if (field == value) {
                return
            }

            field = value
            objective.setDisplayName(value.take(RuntimeTextConstants.LEGACY_SCOREBOARD_TITLE_LIMIT))
        }

    init {
        this.objective = getOrCreateObjective()
        this.title = title
        this.objective.displaySlot = DisplaySlot.SIDEBAR
    }

    override fun get(index: Int) = lines[index]

    override fun set(index: Int, text: String) {
        lines[index] = text

        val team = getOrCreateTeam(index)
        val prefix = text.take(RuntimeTextConstants.LEGACY_SCOREBOARD_TEAM_TEXT_LIMIT)
        val suffix = text.drop(RuntimeTextConstants.LEGACY_SCOREBOARD_TEAM_TEXT_LIMIT).take(RuntimeTextConstants.LEGACY_SCOREBOARD_TEAM_TEXT_LIMIT)

        team.setPrefix(prefix)
        team.setSuffix(suffix)
        objective.getScore(getTeamName(index)).score = RuntimeTextConstants.sidebarScore(index)
    }

    override fun remove(index: Int) {
        val existed = lines.remove(index) != null

        if (existed) {
            val teamName = getTeamName(index)
            scoreboard.resetScores(teamName)
            scoreboard.getTeam(teamName)?.unregister()
        }
    }

    private fun getTeamName(index: Int): String = RuntimeTextConstants.scoreboardEntry(index)

    private fun getOrCreateObjective(): Objective {
        val name = generateRandomString()

        return scoreboard.getObjective(name) ?: scoreboard.registerNewObjective(name, RuntimeTextConstants.SCOREBOARD_OBJECTIVE_CRITERIA)
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

private data class LegacyTitleTicks(
    val fadeIn: Int,
    val stay: Int,
    val fadeOut: Int
) {
    companion object {
        fun from(times: Title.Times): LegacyTitleTicks {
            return LegacyTitleTicks(
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
