import de.comahe.i18n4k.Locale
import dev.tarkan.titlemanager.bukkit.command.CommandContext
import dev.tarkan.titlemanager.bukkit.command.CommandParameters
import dev.tarkan.titlemanager.bukkit.command.subcommands.ReloadSubCommand
import dev.tarkan.titlemanager.bukkit.configuration.ConfigurationException
import dev.tarkan.titlemanager.bukkit.lifecycle.TitleManagerReloader
import dev.tarkan.titlemanager.bukkit.text.ComponentSerializer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReloadSubCommandTest {
    @Test
    fun `reload command reports successful reload`() = runTest {
        val messages = mutableListOf<Component>()
        val sender = senderCapturing(messages)
        val reloader = mockk<TitleManagerReloader>()
        every { reloader.reload() } returns Unit

        ReloadSubCommand(reloader).executeCommand(sender, emptyArray(), CommandParameters(emptyList()), commandContext())

        verify(exactly = 1) { reloader.reload() }
        assertEquals(2, messages.size)
        assertTrue(ComponentSerializer.serialize(messages[0]).contains("Reloading TitleManager configuration"))
        assertTrue(ComponentSerializer.serialize(messages[1]).contains("reload complete"))
    }

    @Test
    fun `reload command reports configuration failure without sending success`() = runTest {
        val messages = mutableListOf<Component>()
        val sender = senderCapturing(messages)
        val reloader = mockk<TitleManagerReloader>()
        every { reloader.reload() } throws ConfigurationException("Invalid config")

        ReloadSubCommand(reloader).executeCommand(sender, emptyArray(), CommandParameters(emptyList()), commandContext())

        verify(exactly = 1) { reloader.reload() }
        assertEquals(2, messages.size)
        assertTrue(ComponentSerializer.serialize(messages[0]).contains("Reloading TitleManager configuration"))
        assertTrue(ComponentSerializer.serialize(messages[1]).contains("reload failed"))
        assertTrue(ComponentSerializer.serialize(messages[1]).contains("Invalid config"))
    }

    @Test
    fun `reload command suppresses success messages when silent`() = runTest {
        val messages = mutableListOf<Component>()
        val sender = senderCapturing(messages)
        val reloader = mockk<TitleManagerReloader>()
        every { reloader.reload() } returns Unit

        ReloadSubCommand(reloader).executeCommand(sender, emptyArray(), CommandParameters.fromArguments(listOf("-silent")), commandContext())

        verify(exactly = 1) { reloader.reload() }
        assertEquals(0, messages.size)
    }

    @Test
    fun `reload command still reports configuration failure when silent`() = runTest {
        val messages = mutableListOf<Component>()
        val sender = senderCapturing(messages)
        val reloader = mockk<TitleManagerReloader>()
        every { reloader.reload() } throws ConfigurationException("Invalid config")

        ReloadSubCommand(reloader).executeCommand(sender, emptyArray(), CommandParameters.fromArguments(listOf("-silent")), commandContext())

        verify(exactly = 1) { reloader.reload() }
        assertEquals(1, messages.size)
        assertTrue(ComponentSerializer.serialize(messages.single()).contains("reload failed"))
        assertTrue(ComponentSerializer.serialize(messages.single()).contains("Invalid config"))
    }

    private fun senderCapturing(messages: MutableList<Component>): CommandSender {
        val sender = mockk<CommandSender>(relaxed = true)

        every { sender.sendMessage(any<Component>()) } answers {
            messages += firstArg<Component>()
        }

        return sender
    }

    private fun commandContext() = CommandContext(Locale.forLanguageTag("en-US"))
}
