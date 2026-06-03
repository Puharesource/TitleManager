package dev.tarkan.titlemanager.nms.direct.v1_20_R2

import dev.tarkan.titlemanager.bukkit.diagnostics.DiagnosticsStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapability
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapabilityStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeSidebar
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeServerVersion
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeVersionModule
import dev.tarkan.titlemanager.bukkit.runtime.RuntimeTextConstants
import dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi.BukkitApiRuntimeAdapter
import net.kyori.adventure.text.Component as AdventureComponent
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.title.Title
import net.minecraft.network.chat.Component as VanillaComponent
import net.minecraft.network.protocol.Packet
import net.minecraft.server.ServerScoreboard
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket
import net.minecraft.network.protocol.game.ClientboundSetScorePacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
import net.minecraft.network.protocol.game.ClientboundTabListPacket
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.criteria.ObjectiveCriteria
import org.bukkit.Server
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_20_R2.util.CraftChatMessage
import org.bukkit.entity.Player
import java.math.BigInteger
import java.time.Duration
import java.util.Random
import java.util.concurrent.ConcurrentHashMap

class V1_20_R2RuntimeVersionModule(
    private val delegate: RuntimeVersionModule,
    serverVersion: RuntimeServerVersion
) : RuntimeVersionModule by delegate {
    constructor(server: Server, serverVersion: RuntimeServerVersion) : this(
        BukkitApiRuntimeAdapter(server, serverVersion),
        serverVersion
    )

    override val id = "nms-v1_20_R2"
    override val displayName = "$id with direct titles/actionbar/player-list/sidebar (${serverVersion.displayVersion})"
    override val capabilities = delegate.capabilities.map { capability ->
        when (capability.name) {
            RuntimeCapability.TITLES -> DiagnosticsStatus(
                RuntimeCapability.TITLES,
                RuntimeCapabilityStatus.AVAILABLE,
                "direct v1_20_R2 title, subtitle, and timing packets"
            )
            RuntimeCapability.ACTIONBAR -> DiagnosticsStatus(
                RuntimeCapability.ACTIONBAR,
                RuntimeCapabilityStatus.AVAILABLE,
                "direct v1_20_R2 ClientboundSetActionBarTextPacket"
            )
            RuntimeCapability.PLAYER_LIST -> DiagnosticsStatus(
                RuntimeCapability.PLAYER_LIST,
                RuntimeCapabilityStatus.AVAILABLE,
                "direct v1_20_R2 ClientboundTabListPacket"
            )
            RuntimeCapability.SIDEBAR -> DiagnosticsStatus(
                RuntimeCapability.SIDEBAR,
                RuntimeCapabilityStatus.AVAILABLE,
                "direct v1_20_R2 scoreboard packets"
            )
            RuntimeCapability.DIRECT_NMS -> DiagnosticsStatus(
                RuntimeCapability.DIRECT_NMS,
                RuntimeCapabilityStatus.AVAILABLE,
                "direct=[titles, actionbar, player-list, sidebar]"
            )
            else -> capability
        }
    }

    override fun sendTitleTimes(player: Player, times: Title.Times) {
        val packetSink = player.packetSinkOrNull()
        if (packetSink == null) {
            delegate.sendTitleTimes(player, times)
            return
        }

        packetSink.sendTitleTimes(times)
    }

    override fun sendTitle(player: Player, title: AdventureComponent) {
        val packetSink = player.packetSinkOrNull()
        if (packetSink == null) {
            delegate.sendTitle(player, title)
            return
        }

        packetSink.sendTitle(title)
    }

    override fun sendSubtitle(player: Player, subtitle: AdventureComponent) {
        val packetSink = player.packetSinkOrNull()
        if (packetSink == null) {
            delegate.sendSubtitle(player, subtitle)
            return
        }

        packetSink.sendSubtitle(subtitle)
    }

    override fun showTitle(player: Player, title: Title) {
        val packetSink = player.packetSinkOrNull()
        if (packetSink == null) {
            delegate.showTitle(player, title)
            return
        }

        packetSink.showTitle(title)
    }

    override fun sendActionBar(player: Player, actionBar: AdventureComponent) {
        val packetSink = player.packetSinkOrNull()
        if (packetSink == null) {
            delegate.sendActionBar(player, actionBar)
            return
        }

        packetSink.sendActionBar(actionBar)
    }

    override fun sendPlayerListHeaderAndFooter(
        player: Player,
        header: AdventureComponent,
        footer: AdventureComponent
    ) {
        val packetSink = player.packetSinkOrNull()
        if (packetSink == null) {
            delegate.sendPlayerListHeaderAndFooter(player, header, footer)
            return
        }

        packetSink.sendPlayerListHeaderAndFooter(header, footer)
    }

    override fun createSidebar(player: Player): RuntimeSidebar {
        val connection = player.connectionOrNull() ?: return delegate.createSidebar(player)

        return V1_20_R2RuntimeSidebar(player, sendPacket = { packet -> connection.send(packet) })
    }

    private fun Player.connectionOrNull(): ServerGamePacketListenerImpl? {
        return (this as? CraftPlayer)?.handle?.connection
    }

    private fun Player.packetSinkOrNull(): V1_20_R2RuntimePacketSink? {
        val connection = connectionOrNull() ?: return null

        return V1_20_R2RuntimePacketSink { packet -> connection.send(packet) }
    }

}

internal class V1_20_R2RuntimePacketSink(
    private val sendPacket: (Packet<*>) -> Unit
) {
    private val componentSerializer = GsonComponentSerializer.gson()

    fun sendTitleTimes(times: Title.Times) {
        sendPacket(ClientboundSetTitlesAnimationPacket(
            times.fadeIn().toTitleTicks(),
            times.stay().toTitleTicks(),
            times.fadeOut().toTitleTicks()
        ))
    }

    fun sendTitle(title: AdventureComponent) {
        sendPacket(ClientboundSetTitleTextPacket(title.toVanillaComponent()))
    }

    fun sendSubtitle(subtitle: AdventureComponent) {
        sendPacket(ClientboundSetSubtitleTextPacket(subtitle.toVanillaComponent()))
    }

    fun showTitle(title: Title) {
        title.times()?.let(::sendTitleTimes)
        sendTitle(title.title())
        sendSubtitle(title.subtitle())
    }

    fun sendActionBar(actionBar: AdventureComponent) {
        sendPacket(ClientboundSetActionBarTextPacket(actionBar.toVanillaComponent()))
    }

    fun sendPlayerListHeaderAndFooter(header: AdventureComponent, footer: AdventureComponent) {
        sendPacket(ClientboundTabListPacket(header.toVanillaComponent(), footer.toVanillaComponent()))
    }

    private fun AdventureComponent.toVanillaComponent(): VanillaComponent {
        return VanillaComponent.Serializer.fromJson(componentSerializer.serialize(this))
            ?: VanillaComponent.empty()
    }

    private fun Duration.toTitleTicks(): Int {
        return toMillis()
            .coerceAtLeast(0)
            .div(50)
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
    }
}

internal class V1_20_R2RuntimeSidebar(
    private val player: Player,
    private val sendPacket: (Packet<*>) -> Unit,
    initialTitle: String = "TitleManager"
) : RuntimeSidebar {
    private val scoreboard = Scoreboard()
    private val objectiveName = generateRandomString()
    private val objective = scoreboard.addObjective(
        objectiveName,
        ObjectiveCriteria.DUMMY,
        initialTitle.toVanillaLegacyComponent(),
        ObjectiveCriteria.RenderType.INTEGER
    )
    private val lines: MutableMap<Int, SidebarLine> = ConcurrentHashMap()
    private var closed = false

    override var title: String = initialTitle
        set(value) {
            if (field == value) {
                return
            }

            field = value
            objective.setDisplayName(value.toVanillaLegacyComponent())
            sendPacket(ClientboundSetObjectivePacket(objective, ClientboundSetObjectivePacket.METHOD_CHANGE))
        }

    init {
        sendPacket(ClientboundSetObjectivePacket(objective, ClientboundSetObjectivePacket.METHOD_ADD))
        sendPacket(ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, objective))
    }

    override fun isAppliedTo(player: Player): Boolean {
        return !closed && player == this.player
    }

    override fun get(index: Int): String? = lines[index]?.value

    override fun set(index: Int, value: String) {
        val existing = lines[index]
        if (existing != null) {
            if (existing.value == value) {
                return
            }

            existing.value = value
            existing.team.setPlayerPrefix(value.toVanillaLegacyComponent())
            sendPacket(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(existing.team, false))
            return
        }

        val entryName = entryName(index)
        val team = scoreboard.addPlayerTeam(teamName(index))
        scoreboard.addPlayerToTeam(entryName, team)
        team.setPlayerPrefix(value.toVanillaLegacyComponent())
        val line = SidebarLine(entryName, team, value)
        lines[index] = line

        sendPacket(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true))
        sendPacket(ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, objective.name, entryName, RuntimeTextConstants.sidebarScore(index)))
    }

    override fun remove(index: Int) {
        val line = lines.remove(index) ?: return

        sendPacket(ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, objective.name, line.entryName, 0))
        sendPacket(ClientboundSetPlayerTeamPacket.createRemovePacket(line.team))
        scoreboard.removePlayerTeam(line.team)
    }

    override fun close() {
        if (closed) {
            return
        }

        lines.keys.toList().forEach(::remove)
        sendPacket(ClientboundSetObjectivePacket(objective, ClientboundSetObjectivePacket.METHOD_REMOVE))
        scoreboard.removeObjective(objective)
        closed = true
    }

    private fun teamName(index: Int): String = "${objectiveName.take(12)}_$index"

    private data class SidebarLine(
        val entryName: String,
        val team: PlayerTeam,
        var value: String
    )

    private companion object {
        private val random = Random()
        private val entries = listOf(
            "§0", "§1", "§2", "§3", "§4",
            "§5", "§6", "§7", "§8", "§9",
            "§a", "§b", "§c", "§d", "§e"
        )

        private fun generateRandomString(): String = "tm" + BigInteger(50, random).toString(32).take(14)

        private fun entryName(index: Int): String = entries[index - 1]

        private fun String.toVanillaLegacyComponent(): VanillaComponent {
            return CraftChatMessage.fromStringOrNull(this) ?: VanillaComponent.empty()
        }
    }
}
