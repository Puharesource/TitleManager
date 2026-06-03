package dev.tarkan.titlemanager.bukkit.diagnostics

import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Server
import org.bukkit.entity.Player

interface RuntimeVersionModuleFactory {
    val id: String
    val priority: Int
        get() = 0

    fun isCompatible(serverVersion: RuntimeServerVersion): Boolean

    fun create(server: Server, serverVersion: RuntimeServerVersion): RuntimeVersionModule
}

abstract class ExactNmsRuntimeVersionModuleFactory(
    override val id: String,
    private val supportedNmsVersion: String
) : RuntimeVersionModuleFactory {
    override fun isCompatible(serverVersion: RuntimeServerVersion): Boolean {
        return serverVersion.matchesNmsVersion(supportedNmsVersion)
    }
}

data class RuntimeServerVersion(
    val bukkitVersion: String,
    val minecraftVersion: String?,
    val craftBukkitPackage: String?,
    val nmsVersion: String?
) {
    val displayVersion: String = listOfNotNull(minecraftVersion, nmsVersion).distinct().joinToString(" / ")
        .ifEmpty { bukkitVersion }

    fun matchesNmsVersion(version: String): Boolean = nmsVersion == version

    companion object {
        fun from(server: Server): RuntimeServerVersion {
            val packageName = server::class.java.`package`?.name
            val nmsVersion = packageName
                ?.split('.')
                ?.firstOrNull { it.matches(Regex("v\\d+_\\d+_R\\d+")) }

            return RuntimeServerVersion(
                bukkitVersion = server.bukkitVersion,
                minecraftVersion = parseMinecraftVersion(server.bukkitVersion),
                craftBukkitPackage = packageName,
                nmsVersion = nmsVersion
            )
        }

        fun parseMinecraftVersion(bukkitVersion: String): String? {
            return Regex("""\b\d+\.\d+(?:\.\d+)?\b""")
                .find(bukkitVersion)
                ?.value
        }
    }
}

interface RuntimeVersionModule {
    val id: String
    val displayName: String
    val capabilities: List<DiagnosticsStatus>
    val threadingPolicy: RuntimeThreadingPolicy

    fun sendTitleTimes(player: Player, times: Title.Times)

    fun sendTitle(player: Player, title: Component)

    fun sendSubtitle(player: Player, subtitle: Component)

    fun showTitle(player: Player, title: Title)

    fun sendActionBar(player: Player, actionBar: Component)

    fun sendPlayerListHeaderAndFooter(player: Player, header: Component, footer: Component)

    fun createSidebar(player: Player): RuntimeSidebar

    fun close() = Unit
}

class UnsupportedRuntimeVersionModule(
    serverVersion: RuntimeServerVersion,
    private val reason: String
) : RuntimeVersionModule {
    override val id = "unsupported"
    override val displayName = "$id (${serverVersion.displayVersion})"
    override val capabilities = listOf(
        DiagnosticsStatus(RuntimeCapability.TITLES, RuntimeCapabilityStatus.UNAVAILABLE, reason),
        DiagnosticsStatus(RuntimeCapability.ACTIONBAR, RuntimeCapabilityStatus.UNAVAILABLE, reason),
        DiagnosticsStatus(RuntimeCapability.PLAYER_LIST, RuntimeCapabilityStatus.UNAVAILABLE, reason),
        DiagnosticsStatus(RuntimeCapability.SIDEBAR, RuntimeCapabilityStatus.UNAVAILABLE, reason),
        DiagnosticsStatus(RuntimeCapability.DIRECT_NMS, RuntimeCapabilityStatus.UNAVAILABLE, reason)
    )
    override val threadingPolicy = RuntimeThreadingPolicy.mainThreadOnly()

    override fun sendTitleTimes(player: Player, times: Title.Times) {
        throw unsupported()
    }

    override fun sendTitle(player: Player, title: Component) {
        throw unsupported()
    }

    override fun sendSubtitle(player: Player, subtitle: Component) {
        throw unsupported()
    }

    override fun showTitle(player: Player, title: Title) {
        throw unsupported()
    }

    override fun sendActionBar(player: Player, actionBar: Component) {
        throw unsupported()
    }

    override fun sendPlayerListHeaderAndFooter(player: Player, header: Component, footer: Component) {
        throw unsupported()
    }

    override fun createSidebar(player: Player): RuntimeSidebar {
        throw unsupported()
    }

    private fun unsupported(): UnsupportedOperationException {
        return UnsupportedOperationException("Runtime version module is unavailable: $reason")
    }
}

data class RuntimeThreadingPolicy(
    val title: String,
    val actionbar: String,
    val playerList: String,
    val sidebar: String
) {
    fun render(): String {
        return "title=$title, actionbar=$actionbar, player-list=$playerList, sidebar=$sidebar"
    }

    companion object {
        fun mainThreadOnly(): RuntimeThreadingPolicy = RuntimeThreadingPolicy(
            title = RuntimeThreadingMode.MAIN_THREAD,
            actionbar = RuntimeThreadingMode.MAIN_THREAD,
            playerList = RuntimeThreadingMode.MAIN_THREAD,
            sidebar = RuntimeThreadingMode.MAIN_THREAD
        )
    }
}

object RuntimeThreadingMode {
    const val MAIN_THREAD = "main-thread"
    const val THREAD_SAFE = "thread-safe"
}

interface RuntimeSidebar {
    var title: String

    fun isAppliedTo(player: Player): Boolean

    fun get(index: Int): String?

    fun set(index: Int, value: String)

    fun remove(index: Int)

    fun close()
}

data class DiagnosticsStatus(
    val name: String,
    val status: String,
    val detail: String
) {
    fun render() = "$name=$status ($detail)"
}

object RuntimeCapability {
    const val TITLES = "titles"
    const val ACTIONBAR = "actionbar"
    const val PLAYER_LIST = "player-list"
    const val SIDEBAR = "sidebar"
    const val DIRECT_NMS = "direct-nms"
    const val RUNTIME_FEATURES = "runtime-features"
    const val COMMANDS = "commands"
    const val COMMAND_SUGGESTIONS = "command-suggestions"
}

object RuntimeCapabilityStatus {
    const val AVAILABLE = "available"
    const val UNAVAILABLE = "unavailable"
}
