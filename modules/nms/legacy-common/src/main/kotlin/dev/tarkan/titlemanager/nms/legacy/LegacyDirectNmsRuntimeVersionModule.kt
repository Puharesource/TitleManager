@file:Suppress("DEPRECATION")

package dev.tarkan.titlemanager.nms.legacy

import dev.tarkan.titlemanager.bukkit.diagnostics.DiagnosticsStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.ExactNmsRuntimeVersionModuleFactory
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapability
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapabilityStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeServerVersion
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeSidebar
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeThreadingPolicy
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeVersionModule
import dev.tarkan.titlemanager.bukkit.runtime.RuntimeCapabilityDetail
import dev.tarkan.titlemanager.bukkit.runtime.RuntimeTextConstants
import dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi.BukkitApiRuntimeAdapter
import dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi.LegacySpigotRuntimeAdapter
import dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi.LegacySpigotTitleOnlyRuntimeAdapter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.title.Title
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import java.math.BigInteger
import java.time.Duration
import java.util.Random
import java.util.concurrent.ConcurrentHashMap

private val legacySerializer = LegacyComponentSerializer.legacySection()

abstract class LegacyDirectNmsRuntimeVersionModuleFactory(
    private val nmsVersion: String,
    private val versionIndex: Int
) : ExactNmsRuntimeVersionModuleFactory("legacy-direct-nms-$nmsVersion", nmsVersion) {
    override val priority: Int = 100

    override fun create(server: Server, serverVersion: RuntimeServerVersion): RuntimeVersionModule {
        return LegacyDirectNmsRuntimeVersionModule(
            server = server,
            serverVersion = serverVersion,
            metadata = LegacyNmsVersion(nmsVersion = nmsVersion, versionIndex = versionIndex),
            packetSink = createPacketSink()
        )
    }

    protected abstract fun createPacketSink(): LegacyDirectNmsPacketSink
}

class LegacyDirectNmsRuntimeVersionModule(
    private val server: Server,
    private val serverVersion: RuntimeServerVersion,
    private val metadata: LegacyNmsVersion,
    private val packetSink: LegacyDirectNmsPacketSink
) : RuntimeVersionModule {
    private val delegate = createApiDelegate()

    override val id = "legacy-direct-nms-${metadata.nmsVersion}"
    override val displayName = "$id (${serverVersion.displayVersion})"
    override val threadingPolicy = RuntimeThreadingPolicy.mainThreadOnly()
    override val capabilities = listOf(
        DiagnosticsStatus(RuntimeCapability.TITLES, RuntimeCapabilityStatus.AVAILABLE, titleCapabilityDetail()),
        DiagnosticsStatus(RuntimeCapability.ACTIONBAR, RuntimeCapabilityStatus.AVAILABLE, actionbarCapabilityDetail()),
        DiagnosticsStatus(RuntimeCapability.PLAYER_LIST, RuntimeCapabilityStatus.AVAILABLE, RuntimeCapabilityDetail.DIRECT_PLAYER_LIST_PACKET),
        DiagnosticsStatus(RuntimeCapability.SIDEBAR, RuntimeCapabilityStatus.AVAILABLE, sidebarCapabilityDetail()),
        DiagnosticsStatus(
            RuntimeCapability.DIRECT_NMS,
            RuntimeCapabilityStatus.AVAILABLE,
            "direct=[${directSurfaces().joinToString()}]; api=[${apiSurfaces().joinToString()}]"
        )
    )

    override fun sendTitleTimes(player: Player, times: Title.Times) {
        if (usesApiTitle()) {
            delegate?.sendTitleTimes(player, times)
            return
        }

        packetSink.sendTitleTimes(player, times.toLegacyTitleTicks())
    }

    override fun sendTitle(player: Player, title: Component) {
        if (usesApiTitle()) {
            delegate?.sendTitle(player, title)
            return
        }

        packetSink.sendTitle(player, legacySerializer.serialize(title), DEFAULT_TITLE_TICKS)
    }

    override fun sendSubtitle(player: Player, subtitle: Component) {
        if (usesApiTitle()) {
            delegate?.sendSubtitle(player, subtitle)
            return
        }

        packetSink.sendSubtitle(player, legacySerializer.serialize(subtitle), DEFAULT_TITLE_TICKS)
    }

    override fun showTitle(player: Player, title: Title) {
        if (usesApiTitle()) {
            delegate?.showTitle(player, title)
            return
        }

        val ticks = (title.times() ?: DEFAULT_TITLE_TIMES).toLegacyTitleTicks()
        packetSink.sendTitleTimes(player, ticks)
        packetSink.sendTitle(player, legacySerializer.serialize(title.title()), ticks)
        packetSink.sendSubtitle(player, legacySerializer.serialize(title.subtitle()), ticks)
    }

    override fun sendActionBar(player: Player, actionBar: Component) {
        if (usesApiActionbar()) {
            delegate?.sendActionBar(player, actionBar)
            return
        }

        packetSink.sendActionbar(player, legacySerializer.serialize(actionBar))
    }

    override fun sendPlayerListHeaderAndFooter(player: Player, header: Component, footer: Component) {
        packetSink.sendPlayerListHeaderAndFooter(
            player,
            legacySerializer.serialize(header),
            legacySerializer.serialize(footer)
        )
    }

    override fun createSidebar(player: Player): RuntimeSidebar {
        return delegate?.createSidebar(player) ?: LegacyDirectNmsRuntimeSidebar(server, player, LegacyDirectNmsScoreboardHandler(server.scoreboardManager.newScoreboard))
    }

    override fun close() {
        delegate?.close()
    }

    private fun createApiDelegate(): RuntimeVersionModule? {
        val version = serverVersion.minecraftVersion ?: return null
        val parts = version.split('.').mapNotNull { it.toIntOrNull() }
        val major = parts.getOrNull(0) ?: return null
        val minor = parts.getOrNull(1) ?: return null

        if (major != 1) {
            return null
        }

        return when (minor) {
            in 17..Int.MAX_VALUE -> BukkitApiRuntimeAdapter(server, serverVersion)
            in 12..16 -> LegacySpigotRuntimeAdapter(server, serverVersion)
            in 8..11 -> LegacySpigotTitleOnlyRuntimeAdapter(server, serverVersion)
            else -> null
        }
    }

    private fun usesApiTitle(): Boolean = metadata.versionIndex >= 6

    private fun usesApiActionbar(): Boolean = metadata.versionIndex >= 6

    private fun titleCapabilityDetail(): String = if (usesApiTitle()) {
        RuntimeCapabilityDetail.SPIGOT_TITLE_API
    } else {
        RuntimeCapabilityDetail.DIRECT_TITLE_PACKET
    }

    private fun actionbarCapabilityDetail(): String = if (usesApiActionbar()) {
        RuntimeCapabilityDetail.SPIGOT_CHAT_MESSAGE_TYPE_API
    } else {
        RuntimeCapabilityDetail.DIRECT_ACTIONBAR_PACKET
    }

    private fun sidebarCapabilityDetail(): String = delegate?.capabilities
        ?.firstOrNull { it.name == RuntimeCapability.SIDEBAR }
        ?.detail
        ?: RuntimeCapabilityDetail.BUKKIT_SCOREBOARD_STRING_API

    private fun directSurfaces(): List<String> = buildList {
        if (!usesApiTitle()) add(RuntimeCapability.TITLES)
        if (!usesApiActionbar()) add(RuntimeCapability.ACTIONBAR)
        add(RuntimeCapability.PLAYER_LIST)
    }

    private fun apiSurfaces(): List<String> = buildList {
        if (usesApiTitle()) add(RuntimeCapability.TITLES)
        if (usesApiActionbar()) add(RuntimeCapability.ACTIONBAR)
        add(RuntimeCapability.SIDEBAR)
    }

    companion object {
        private val DEFAULT_TITLE_TICKS = LegacyTitleTicks(10, 70, 20)
        private val DEFAULT_TITLE_TIMES = Title.Times.times(
            Duration.ofMillis(500),
            Duration.ofMillis(3500),
            Duration.ofMillis(1000)
        )
    }
}

data class LegacyNmsVersion(
    val nmsVersion: String,
    val versionIndex: Int
)

data class LegacyTitleTicks(
    val fadeIn: Int,
    val stay: Int,
    val fadeOut: Int
)

interface LegacyDirectNmsPacketSink {
    fun sendTitleTimes(player: Player, ticks: LegacyTitleTicks)

    fun sendTitle(player: Player, title: String, ticks: LegacyTitleTicks)

    fun sendSubtitle(player: Player, subtitle: String, ticks: LegacyTitleTicks)

    fun sendActionbar(player: Player, text: String)

    fun sendPlayerListHeaderAndFooter(player: Player, header: String, footer: String)
}

fun legacyComponentJson(text: String): String = GsonComponentSerializer.gson().serialize(legacySerializer.deserialize(text))

private fun Title.Times.toLegacyTitleTicks(): LegacyTitleTicks {
    return LegacyTitleTicks(
        fadeIn = fadeIn().toTicks(),
        stay = stay().toTicks(),
        fadeOut = fadeOut().toTicks()
    )
}

private fun Duration.toTicks(): Int {
    return (toMillis() / 50).coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
}

private class LegacyDirectNmsRuntimeSidebar(
    private val server: Server,
    private val player: Player,
    private val scoreboardHandler: LegacyDirectNmsScoreboardHandler
) : RuntimeSidebar {
    init {
        player.scoreboard = scoreboardHandler.scoreboard
    }

    override fun isAppliedTo(player: Player): Boolean = this.player.uniqueId == player.uniqueId && player.scoreboard == scoreboardHandler.scoreboard

    override var title: String
        get() = scoreboardHandler.title
        set(value) {
            scoreboardHandler.title = value
        }

    override fun set(index: Int, value: String) {
        scoreboardHandler.set(index, value)
    }

    override fun get(index: Int): String? = scoreboardHandler.get(index)

    override fun remove(index: Int) {
        scoreboardHandler.remove(index)
    }

    override fun close() {
        scoreboardHandler.close()
        player.scoreboard = server.scoreboardManager.mainScoreboard
    }
}

private class LegacyDirectNmsScoreboardHandler(
    val scoreboard: Scoreboard,
    title: String = RuntimeTextConstants.SCOREBOARD_DEFAULT_TITLE,
    private val lines: MutableMap<Int, String> = ConcurrentHashMap()
) {
    private val objectiveName = "tm-${BigInteger(32, Random()).toString(32)}".take(RuntimeTextConstants.LEGACY_SCOREBOARD_TEAM_TEXT_LIMIT)
    private val objective: Objective = scoreboard.registerNewObjective(objectiveName, RuntimeTextConstants.SCOREBOARD_OBJECTIVE_CRITERIA, title.take(RuntimeTextConstants.LEGACY_SCOREBOARD_TITLE_LIMIT)).also {
        it.displaySlot = DisplaySlot.SIDEBAR
    }

    var title: String
        get() = objective.displayName
        set(value) {
            objective.displayName = value.take(RuntimeTextConstants.LEGACY_SCOREBOARD_TITLE_LIMIT)
        }

    fun set(index: Int, text: String) {
        val normalized = text.take(RuntimeTextConstants.LEGACY_SCOREBOARD_TITLE_LIMIT)
        val current = lines[index]
        if (current == normalized) {
            return
        }
        if (current != null) {
            remove(index)
        }

        val team = team(index)
        val entry = entry(index)
        team.prefix = normalized.take(RuntimeTextConstants.LEGACY_SCOREBOARD_TEAM_TEXT_LIMIT)
        team.suffix = normalized.drop(RuntimeTextConstants.LEGACY_SCOREBOARD_TEAM_TEXT_LIMIT).take(RuntimeTextConstants.LEGACY_SCOREBOARD_TEAM_TEXT_LIMIT)
        team.addEntry(entry)
        objective.getScore(entry).score = RuntimeTextConstants.sidebarScore(index)
        lines[index] = normalized
    }

    fun get(index: Int): String? = lines[index]

    fun remove(index: Int) {
        lines.remove(index) ?: return
        val entry = entry(index)
        scoreboard.resetScores(entry)
        team(index).unregister()
    }

    fun close() {
        lines.keys.toList().forEach(::remove)
        objective.unregister()
    }

    private fun team(index: Int): Team {
        val teamName = "tm$index".take(RuntimeTextConstants.LEGACY_SCOREBOARD_TEAM_TEXT_LIMIT)
        return scoreboard.getTeam(teamName) ?: scoreboard.registerNewTeam(teamName)
    }

    private fun entry(index: Int): String {
        return RuntimeTextConstants.legacyScoreboardEntry(index)
    }
}

