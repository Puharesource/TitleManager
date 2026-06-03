import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeServerVersion
import dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi.BukkitApiRuntimeAdapter
import dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi.LegacySpigotRuntimeAdapter
import dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi.LegacySpigotTitleOnlyRuntimeAdapter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart

import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.entity.PlayerMock
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BukkitApiRuntimeAdapterTest {
    @Test
    fun `reports Bukkit API module identity`() {
        val server = MockBukkit.mock()

        try {
            val module = BukkitApiRuntimeAdapter(server)
            val serverVersion = RuntimeServerVersion.from(server)

            assertEquals("bukkit-api", module.id)
            assertEquals("bukkit-api (${serverVersion.displayVersion})", module.displayName)
            assertEquals(true, BukkitApiRuntimeAdapter.isCompatible(serverVersion))
            assertEquals(
                false,
                BukkitApiRuntimeAdapter.isCompatible(RuntimeServerVersion("git-Spigot-test (MC: 1.8.8)", "1.8.8", "org.bukkit.craftbukkit.v1_8_R3", "v1_8_R3"))
            )
            assertEquals(
                "title=main-thread, actionbar=main-thread, player-list=main-thread, sidebar=main-thread",
                module.threadingPolicy.render()
            )
        } finally {
            MockBukkit.unmock()
        }
    }

    @Test
    fun `routes title actionbar and player-list calls through Bukkit player API`() {
        val server = MockBukkit.mock()

        try {
            val module = BukkitApiRuntimeAdapter(server)
            val player = CapturingPlayer(server, "Target")
            val times = Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)
            val title = Component.text("Title")
            val subtitle = Component.text("Subtitle")
            val actionbar = Component.text("Actionbar")
            val footer = Component.text("Footer")
            val fullTitle = Title.title(title, subtitle, times)

            module.sendTitleTimes(player, times)
            module.sendTitle(player, title)
            module.sendSubtitle(player, subtitle)
            module.showTitle(player, fullTitle)
            module.sendActionBar(player, actionbar)
            module.sendPlayerListHeaderAndFooter(player, title, footer)

            assertEquals(times, player.times)
            assertEquals("Title", player.titleParts.single())
            assertEquals("Subtitle", player.subtitleParts.single())
            assertEquals(CapturedTitle("Title", "Subtitle"), player.titles.single())
            assertEquals("Actionbar", player.actionBars.single())
            assertEquals(CapturedPlayerList("Title", "Footer"), player.playerLists.single())
        } finally {
            MockBukkit.unmock()
        }
    }

    @Test
    fun `creates and updates Bukkit API sidebars`() {
        val server = MockBukkit.mock()

        try {
            val module = BukkitApiRuntimeAdapter(server)
            val player = CapturingPlayer(server, "Target")
            val sidebar = module.createSidebar(player)

            assertEquals(true, sidebar.isAppliedTo(player))

            sidebar.title = "Board"
            sidebar.set(1, "Line one")
            val lineEntry = "§1"

            assertEquals("Board", sidebar.title)
            assertEquals("Line one", sidebar.get(1))
            assertEquals(true, player.scoreboard.entries.contains(lineEntry))
            assertEquals(true, player.scoreboard.getTeam(lineEntry)?.hasEntry(lineEntry))
            assertEquals(0, player.scoreboard.getScores(lineEntry).single().score)
            sidebar.set(2, "Line two")
            assertEquals(1, player.scoreboard.getScores("§2").single().score)

            sidebar.remove(1)
            assertEquals(null, sidebar.get(1))
            assertEquals(false, player.scoreboard.entries.contains(lineEntry))
            assertEquals(null, player.scoreboard.getTeam(lineEntry))
            assertEquals(true, sidebar.isAppliedTo(player))

            sidebar.close()
            assertEquals(server.scoreboardManager.mainScoreboard, player.scoreboard)
            assertEquals(false, sidebar.isAppliedTo(player))
        } finally {
            MockBukkit.unmock()
        }
    }

    @Test
    fun `legacy Spigot module reports partial old-server API capabilities`() {
        val server = MockBukkit.mock()

        try {
            val runtimeVersion = RuntimeServerVersion(
                bukkitVersion = "git-Spigot-test (MC: 1.12.2)",
                minecraftVersion = "1.12.2",
                craftBukkitPackage = "org.bukkit.craftbukkit.v1_12_R1",
                nmsVersion = "v1_12_R1"
            )
            val module = LegacySpigotRuntimeAdapter(server, runtimeVersion)

            assertEquals("legacy-spigot-api", module.id)
            assertEquals(true, LegacySpigotRuntimeAdapter.isCompatible(runtimeVersion))
            assertEquals(false, LegacySpigotRuntimeAdapter.isCompatible(RuntimeServerVersion("git-Spigot-test (MC: 1.8.8)", "1.8.8", "org.bukkit.craftbukkit.v1_8_R3", "v1_8_R3")))
            assertEquals("available", module.capabilities.single { it.name == "titles" }.status)
            assertEquals("available", module.capabilities.single { it.name == "actionbar" }.status)
            assertEquals("unavailable", module.capabilities.single { it.name == "player-list" }.status)
            assertEquals("available", module.capabilities.single { it.name == "sidebar" }.status)
            assertEquals("unavailable", module.capabilities.single { it.name == "direct-nms" }.status)
        } finally {
            MockBukkit.unmock()
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun `legacy Spigot module routes title calls through string title API and rejects player-list`() {
        val server = MockBukkit.mock()

        try {
            val runtimeVersion = RuntimeServerVersion(
                bukkitVersion = "git-Spigot-test (MC: 1.12.2)",
                minecraftVersion = "1.12.2",
                craftBukkitPackage = "org.bukkit.craftbukkit.v1_12_R1",
                nmsVersion = "v1_12_R1"
            )
            val module = LegacySpigotRuntimeAdapter(server, runtimeVersion)
            val player = CapturingPlayer(server, "Target")
            val times = Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(1000), Duration.ofMillis(200))

            module.sendTitleTimes(player, times)
            module.sendTitle(player, Component.text("Legacy Title"))
            module.sendSubtitle(player, Component.text("Legacy Subtitle"))
            module.showTitle(player, Title.title(Component.text("Full Title"), Component.text("Full Subtitle"), times))

            assertEquals(
                listOf(
                    CapturedLegacyTitle("Legacy Title", "", 2, 20, 4),
                    CapturedLegacyTitle("", "Legacy Subtitle", 2, 20, 4),
                    CapturedLegacyTitle("Full Title", "Full Subtitle", 2, 20, 4)
                ),
                player.legacyTitles.toList()
            )
            assertFailsWith<UnsupportedOperationException> {
                module.sendPlayerListHeaderAndFooter(player, Component.text("Header"), Component.text("Footer"))
            }

            val sidebar = module.createSidebar(player)
            sidebar.title = "Legacy Sidebar Title That Is Longer Than Old Limits"
            sidebar.set(1, "123456789012345678901234567890123")
            val lineEntry = "§1"

            assertEquals(true, sidebar.isAppliedTo(player))
            assertEquals(true, player.scoreboard.entries.contains(lineEntry))
            val team = player.scoreboard.getTeam(lineEntry)
            assertEquals("1234567890123456", team?.prefix)
            assertEquals("7890123456789012", team?.suffix)
            assertEquals("123456789012345678901234567890123", sidebar.get(1))

            sidebar.remove(1)
            assertEquals(null, sidebar.get(1))
            assertEquals(false, player.scoreboard.entries.contains(lineEntry))
            assertEquals(null, player.scoreboard.getTeam(lineEntry))

            sidebar.set(1, "Restored")
            sidebar.close()
            assertEquals(server.scoreboardManager.mainScoreboard, player.scoreboard)
            assertEquals(false, sidebar.isAppliedTo(player))
        } finally {
            MockBukkit.unmock()
        }
    }

    @Test
    fun `title-only legacy Spigot module reports oldest fallback capabilities`() {
        val server = MockBukkit.mock()

        try {
            val runtimeVersion = RuntimeServerVersion(
                bukkitVersion = "git-Spigot-test (MC: 1.8.8)",
                minecraftVersion = "1.8.8",
                craftBukkitPackage = "org.bukkit.craftbukkit.v1_8_R3",
                nmsVersion = "v1_8_R3"
            )
            val module = LegacySpigotTitleOnlyRuntimeAdapter(server, runtimeVersion)

            assertEquals("legacy-spigot-title-api", module.id)
            assertEquals(true, LegacySpigotTitleOnlyRuntimeAdapter.isCompatible(runtimeVersion))
            assertEquals("available", module.capabilities.single { it.name == "titles" }.status)
            assertEquals("unavailable", module.capabilities.single { it.name == "actionbar" }.status)
            assertEquals("unavailable", module.capabilities.single { it.name == "player-list" }.status)
            assertEquals("available", module.capabilities.single { it.name == "sidebar" }.status)
            assertEquals("unavailable", module.capabilities.single { it.name == "direct-nms" }.status)
        } finally {
            MockBukkit.unmock()
        }
    }

    @Test
    fun `title-only legacy Spigot module routes untimed titles and rejects missing APIs`() {
        val server = MockBukkit.mock()

        try {
            val runtimeVersion = RuntimeServerVersion(
                bukkitVersion = "git-Spigot-test (MC: 1.8.8)",
                minecraftVersion = "1.8.8",
                craftBukkitPackage = "org.bukkit.craftbukkit.v1_8_R3",
                nmsVersion = "v1_8_R3"
            )
            val module = LegacySpigotTitleOnlyRuntimeAdapter(server, runtimeVersion)
            val player = CapturingPlayer(server, "Target")

            module.sendTitle(player, Component.text("Old Title"))
            module.sendSubtitle(player, Component.text("Old Subtitle"))
            module.showTitle(player, Title.title(Component.text("Full Old Title"), Component.text("Full Old Subtitle")))

            assertEquals(
                listOf(
                    CapturedLegacyTitle("Old Title", "", null, null, null),
                    CapturedLegacyTitle("", "Old Subtitle", null, null, null),
                    CapturedLegacyTitle("Full Old Title", "Full Old Subtitle", null, null, null)
                ),
                player.legacyTitles.toList()
            )
            assertFailsWith<UnsupportedOperationException> {
                module.sendActionBar(player, Component.text("Actionbar"))
            }
            assertFailsWith<UnsupportedOperationException> {
                module.sendPlayerListHeaderAndFooter(player, Component.text("Header"), Component.text("Footer"))
            }

            val sidebar = module.createSidebar(player)
            val lineEntry = "§2"
            sidebar.set(2, "Old Sidebar")

            assertEquals(true, sidebar.isAppliedTo(player))
            assertEquals(true, player.scoreboard.entries.contains(lineEntry))

            sidebar.remove(2)
            assertEquals(null, sidebar.get(2))
            assertEquals(false, player.scoreboard.entries.contains(lineEntry))
            assertEquals(null, player.scoreboard.getTeam(lineEntry))

            sidebar.set(2, "Restored")
            sidebar.close()
            assertEquals(server.scoreboardManager.mainScoreboard, player.scoreboard)
            assertEquals(false, sidebar.isAppliedTo(player))
        } finally {
            MockBukkit.unmock()
        }
    }

    private data class CapturedTitle(val title: String, val subtitle: String)

    private data class CapturedPlayerList(val header: String, val footer: String)

    private data class CapturedLegacyTitle(
        val title: String,
        val subtitle: String,
        val fadeIn: Int?,
        val stay: Int?,
        val fadeOut: Int?
    )

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    private class CapturingPlayer(server: org.mockbukkit.mockbukkit.ServerMock, name: String) : PlayerMock(server, name, UUID.randomUUID()) {
        var times: Title.Times? = null
        val titleParts: ConcurrentLinkedQueue<String> = ConcurrentLinkedQueue()
        val subtitleParts: ConcurrentLinkedQueue<String> = ConcurrentLinkedQueue()
        val titles: ConcurrentLinkedQueue<CapturedTitle> = ConcurrentLinkedQueue()
        val legacyTitles: ConcurrentLinkedQueue<CapturedLegacyTitle> = ConcurrentLinkedQueue()
        val playerLists: ConcurrentLinkedQueue<CapturedPlayerList> = ConcurrentLinkedQueue()
        val actionBars: ConcurrentLinkedQueue<String> = ConcurrentLinkedQueue()
        private val serializer = PlainTextComponentSerializer.plainText()

        override fun <T : Any> sendTitlePart(part: TitlePart<T>, value: T) {
            when (part) {
                TitlePart.TIMES -> times = value as Title.Times
                TitlePart.TITLE -> titleParts.add(serializer.serialize(value as Component))
                TitlePart.SUBTITLE -> subtitleParts.add(serializer.serialize(value as Component))
            }
        }

        override fun showTitle(title: Title) {
            titles.add(CapturedTitle(
                title = serializer.serialize(title.title()),
                subtitle = serializer.serialize(title.subtitle())
            ))
        }

        override fun sendTitle(title: String?, subtitle: String?, fadeIn: Int, stay: Int, fadeOut: Int) {
            legacyTitles.add(CapturedLegacyTitle(title.orEmpty(), subtitle.orEmpty(), fadeIn, stay, fadeOut))
        }

        override fun sendTitle(title: String?, subtitle: String?) {
            legacyTitles.add(CapturedLegacyTitle(title.orEmpty(), subtitle.orEmpty(), null, null, null))
        }

        override fun sendPlayerListHeaderAndFooter(header: Component, footer: Component) {
            playerLists.add(CapturedPlayerList(
                header = serializer.serialize(header),
                footer = serializer.serialize(footer)
            ))
        }

        override fun sendActionBar(message: Component) {
            actionBars.add(serializer.serialize(message))
        }
    }
}
