import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.command.CommandContext
import dev.tarkan.titlemanager.bukkit.command.CommandParameters
import dev.tarkan.titlemanager.bukkit.command.actionbar.ActionbarMessageSubCommand
import dev.tarkan.titlemanager.bukkit.command.title.TitleMessageSubCommand
import dev.tarkan.titlemanager.bukkit.context.PlayerContextManager
import io.mockk.every
import io.mockk.mockk
import org.bukkit.Server
import org.bukkit.entity.Player
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertContentEquals

class MessageSubCommandTabCompletionTest {
    @Test
    fun `title message tab completion suggests online players by prefix`() {
        withPluginPlayers("Alex", "Steve", "Avery") { plugin, playerContextManager ->
            val command = TitleMessageSubCommand(plugin, playerContextManager)

            assertContentEquals(
                listOf("Alex", "Avery"),
                command.tabComplete(mockk(relaxed = true), arrayOf("A"), CommandParameters(emptyList()), context())
            )
        }
    }

    @Test
    fun `actionbar message tab completion suggests online players by prefix`() {
        withPluginPlayers("Alex", "Steve", "Avery") { plugin, playerContextManager ->
            val command = ActionbarMessageSubCommand(plugin, playerContextManager)

            assertContentEquals(
                listOf("Steve"),
                command.tabComplete(mockk(relaxed = true), arrayOf("st"), CommandParameters(emptyList()), context())
            )
        }
    }

    @Test
    fun `message tab completion stops after target argument`() {
        withPluginPlayers("Alex") { plugin, playerContextManager ->
            val titleCommand = TitleMessageSubCommand(plugin, playerContextManager)
            val actionbarCommand = ActionbarMessageSubCommand(plugin, playerContextManager)

            assertContentEquals(
                emptyList(),
                titleCommand.tabComplete(mockk(relaxed = true), arrayOf("Alex", "Hello"), CommandParameters(emptyList()), context())
            )
            assertContentEquals(
                emptyList(),
                actionbarCommand.tabComplete(mockk(relaxed = true), arrayOf("Alex", "Hello"), CommandParameters(emptyList()), context())
            )
        }
    }

    private fun withPluginPlayers(vararg names: String, block: (TitleManagerPlugin, PlayerContextManager) -> Unit) {
        val plugin = mockk<TitleManagerPlugin>()
        val server = mockk<Server>()
        val players = names.map { name ->
            mockk<Player> {
                every { this@mockk.name } returns name
            }
        }

        every { plugin.server } returns server
        every { server.onlinePlayers } returns players
        val playerContextManager = mockk<PlayerContextManager>(relaxed = true)

        block(plugin, playerContextManager)
    }

    private fun context() = CommandContext(Locale.ENGLISH)
}
