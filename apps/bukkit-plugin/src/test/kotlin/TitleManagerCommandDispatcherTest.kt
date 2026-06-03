import de.comahe.i18n4k.Locale
import dev.tarkan.titlemanager.bukkit.command.CommandContext
import dev.tarkan.titlemanager.bukkit.command.CommandParameters
import dev.tarkan.titlemanager.bukkit.command.TitleManagerCommandDispatcher
import dev.tarkan.titlemanager.bukkit.command.TitleManagerSubCommand
import dev.tarkan.titlemanager.bukkit.command.subcommands.ListScriptsSubCommand
import dev.tarkan.titlemanager.bukkit.concurrency.ConcurrencyType
import dev.tarkan.titlemanager.bukkit.configuration.PlaceholderConfiguration
import dev.tarkan.titlemanager.bukkit.localization.ReloadCommandMessages
import dev.tarkan.titlemanager.bukkit.text.ComponentSerializer
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TitleManagerCommandDispatcherTest {
    @Test
    fun `sends help when no subcommand is supplied`() {
        val messages = mutableListOf<Component>()
        val sender = senderCapturing(messages)
        val subCommand = RecordingSubCommand("version", "v")
        val dispatcher = dispatcher(subCommand)

        assertTrue(dispatcher.onCommand(sender, mockk(), "tm", emptyArray()))

        assertEquals(2, messages.size)
        assertTrue(ComponentSerializer.serialize(messages[0]).contains("Correct usage"))
        assertTrue(ComponentSerializer.serialize(messages[1]).contains("/tm version"))
        assertNull(subCommand.lastCall)
    }

    @Test
    fun `routes subcommands by name case-insensitively and strips leading parameters`() {
        val subCommand = RecordingSubCommand("reload")
        val dispatcher = dispatcher(subCommand)

        assertTrue(dispatcher.onCommand(mockk(relaxed = true), mockk(), "tm", arrayOf("ReLoAd", "-silent=true", "now")))

        val call = subCommand.lastCall!!
        assertContentEquals(arrayOf("now"), call.args)
        assertEquals("true", call.parameters.getValue("silent"))
        assertEquals(Locale.forLanguageTag("da-DK"), call.context.locale)
    }

    @Test
    fun `strips every consumed leading parameter even when names are duplicated`() {
        val subCommand = RecordingSubCommand("reload")
        val dispatcher = dispatcher(subCommand)

        assertTrue(dispatcher.onCommand(mockk(relaxed = true), mockk(), "tm", arrayOf("reload", "-silent", "-silent", "now")))

        val call = subCommand.lastCall!!
        assertContentEquals(arrayOf("now"), call.args)
        assertTrue(call.parameters.isSilent)
        assertEquals(1, call.parameters.size)
        assertEquals(2, call.parameters.consumedArgumentCount)
    }

    @Test
    fun `keeps unknown dash-prefixed arguments in command message`() {
        val subCommand = RecordingSubCommand("broadcast")
        val dispatcher = dispatcher(subCommand)

        assertTrue(dispatcher.onCommand(mockk(relaxed = true), mockk(), "tm", arrayOf("broadcast", "-red", "message")))

        val call = subCommand.lastCall!!
        assertContentEquals(arrayOf("-red", "message"), call.args)
        assertEquals(0, call.parameters.size)
        assertEquals(0, call.parameters.consumedArgumentCount)
    }

    @Test
    fun `routes subcommands by alias case-insensitively`() {
        val subCommand = RecordingSubCommand("version", "v", concurrencyType = ConcurrencyType.ASYNC)
        val runTypes = mutableListOf<ConcurrencyType>()
        val dispatcher = dispatcher(subCommand, runTypes = runTypes)

        assertTrue(dispatcher.onCommand(mockk(relaxed = true), mockk(), "tm", arrayOf("V")))

        assertEquals(ConcurrencyType.ASYNC, runTypes.single())
        assertContentEquals(emptyArray(), subCommand.lastCall!!.args)
    }

    @Test
    fun `uses player locale when sender is a player`() {
        val player = mockk<Player>(relaxed = true)
        every { player.locale() } returns java.util.Locale.CANADA_FRENCH
        val subCommand = RecordingSubCommand("version")
        val dispatcher = dispatcher(subCommand)

        assertTrue(dispatcher.onCommand(player, mockk(), "tm", arrayOf("version")))

        assertEquals(Locale.forLanguageTag("fr-CA"), subCommand.lastCall!!.context.locale)
    }

    @Test
    fun `sends help for unknown subcommands`() {
        val messages = mutableListOf<Component>()
        val sender = senderCapturing(messages)
        val subCommand = RecordingSubCommand("version", "v")
        val dispatcher = dispatcher(subCommand)

        assertTrue(dispatcher.onCommand(sender, mockk(), "tm", arrayOf("missing")))

        assertEquals(2, messages.size)
        assertTrue(ComponentSerializer.serialize(messages[0]).contains("Correct usage"))
        assertTrue(ComponentSerializer.serialize(messages[1]).contains("/tm version"))
        assertNull(subCommand.lastCall)
    }

    @Test
    fun `denies subcommands when sender lacks permission`() {
        val messages = mutableListOf<Component>()
        val sender = senderCapturing(messages, hasPermission = false)
        val subCommand = RecordingSubCommand("reload", permission = "titlemanager.command.reload")
        val dispatcher = dispatcher(subCommand)

        assertTrue(dispatcher.onCommand(sender, mockk(), "tm", arrayOf("reload")))

        assertEquals(1, messages.size)
        assertTrue(ComponentSerializer.serialize(messages.single()).contains("titlemanager.command.reload"))
        assertNull(subCommand.lastCall)
    }

    @Test
    fun `denies player-only subcommands for console senders`() {
        val messages = mutableListOf<Component>()
        val sender = senderCapturing(messages)
        val subCommand = RecordingSubCommand("sidebar", playerOnly = true)
        val dispatcher = dispatcher(subCommand)

        assertTrue(dispatcher.onCommand(sender, mockk(), "tm", arrayOf("sidebar")))

        assertEquals(1, messages.size)
        assertTrue(ComponentSerializer.serialize(messages.single()).contains("only be run as a player"))
        assertNull(subCommand.lastCall)
    }

    @Test
    fun `hides subcommands from help when sender lacks permission`() {
        val messages = mutableListOf<Component>()
        val sender = senderCapturing(messages, hasPermission = false)
        val visible = RecordingSubCommand("version")
        val hidden = RecordingSubCommand("reload", permission = "titlemanager.command.reload")
        val dispatcher = dispatcher(visible, hidden)

        assertTrue(dispatcher.onCommand(sender, mockk(), "tm", emptyArray()))

        assertEquals(2, messages.size)
        val helpText = messages.joinToString("\n") { ComponentSerializer.serialize(it) }
        assertTrue(helpText.contains("/tm version"))
        assertFalse(helpText.contains("/tm reload"))
    }

    @Test
    fun `scripts command reports unsupported scripting through dispatcher`() {
        val messages = mutableListOf<Component>()
        val sender = senderCapturing(messages)
        val dispatcher = dispatcher(ListScriptsSubCommand())

        assertTrue(dispatcher.onCommand(sender, mockk(), "tm", arrayOf("scripts")))

        assertEquals(1, messages.size)
        assertTrue(ComponentSerializer.serialize(messages.single()).contains("Legacy TitleManager scripts are not supported"))
    }

    @Test
    fun `scripts command suppresses informational output when silent`() {
        val messages = mutableListOf<Component>()
        val sender = senderCapturing(messages)
        val dispatcher = dispatcher(ListScriptsSubCommand())

        assertTrue(dispatcher.onCommand(sender, mockk(), "tm", arrayOf("scripts", "-silent")))

        assertEquals(0, messages.size)
    }

    @Test
    fun `scripts command is permission gated in dispatcher help and completion`() {
        val messages = mutableListOf<Component>()
        val sender = senderCapturing(messages, hasPermission = false)
        val dispatcher = dispatcher(ListScriptsSubCommand())

        assertTrue(dispatcher.onCommand(sender, mockk(), "tm", arrayOf("scripts")))

        assertEquals(1, messages.size)
        assertTrue(ComponentSerializer.serialize(messages.single()).contains("titlemanager.command.scripts"))

        messages.clear()
        assertTrue(dispatcher.onCommand(sender, mockk(), "tm", emptyArray()))
        assertFalse(messages.joinToString("\n") { ComponentSerializer.serialize(it) }.contains("/tm scripts"))
        assertContentEquals(emptyList(), dispatcher.onTabComplete(sender, mockk(), "tm", arrayOf("s")))
    }

    @Test
    fun `tab completes visible top-level subcommands and aliases by prefix`() {
        val sender = senderCapturing(mutableListOf())
        val dispatcher = dispatcher(
            RecordingSubCommand("version", "v"),
            RecordingSubCommand("reload", "rl", permission = "titlemanager.command.reload")
        )

        assertContentEquals(
            listOf("reload", "rl"),
            dispatcher.onTabComplete(sender, mockk(), "tm", arrayOf("r"))
        )
    }

    @Test
    fun `tab completion hides top-level subcommands when sender lacks permission`() {
        val sender = senderCapturing(mutableListOf(), hasPermission = false)
        val dispatcher = dispatcher(
            RecordingSubCommand("version", "v"),
            RecordingSubCommand("reload", "rl", permission = "titlemanager.command.reload")
        )

        assertContentEquals(
            listOf("v", "version"),
            dispatcher.onTabComplete(sender, mockk(), "tm", arrayOf(""))
        )
    }

    @Test
    fun `tab completion delegates remaining args and parameters to matched subcommand`() {
        val sender = senderCapturing(mutableListOf())
        val subCommand = CompletingSubCommand("title", "t", completions = listOf("broadcast"))
        val dispatcher = dispatcher(subCommand)

        val completions = dispatcher.onTabComplete(sender, mockk(), "tm", arrayOf("title", "-world=world", "b"))

        assertContentEquals(listOf("broadcast"), completions)
        assertContentEquals(arrayOf("b"), subCommand.lastCompletion!!.args)
        assertEquals("world", subCommand.lastCompletion!!.parameters.getValue("world"))
    }

    @Test
    fun `rich suggestions keep tooltip metadata while legacy tab completion degrades to text`() {
        val sender = senderCapturing(mutableListOf())
        val subCommand = RichCompletingSubCommand("title", "t")
        val dispatcher = dispatcher(subCommand)

        val suggestions = dispatcher.onSuggestions(sender, "tm", arrayOf("title", "b"))

        assertContentEquals(listOf("broadcast"), suggestions.map { it.text })
        assertEquals("Broadcast a title.", ComponentSerializer.serialize(suggestions.single().tooltip!!))
        assertContentEquals(listOf("broadcast"), dispatcher.onTabComplete(sender, mockk(), "tm", arrayOf("title", "b")))
    }

    @Test
    fun `top-level suggestions include localized command descriptions for modern clients`() {
        val sender = senderCapturing(mutableListOf())
        val dispatcher = dispatcher(RecordingSubCommand("reload", "rl"))

        val suggestions = dispatcher.onSuggestions(sender, "tm", arrayOf("r"))

        assertContentEquals(listOf("reload", "rl"), suggestions.map { it.text })
        assertTrue(suggestions.all { it.tooltip != null })
        assertTrue(ComponentSerializer.serialize(suggestions.first { it.text == "reload" }.tooltip!!).contains("Reload"))
    }

    @Test
    fun `parameter suggestions include metadata for modern clients`() {
        val sender = senderCapturing(mutableListOf())
        val dispatcher = dispatcher(RecordingSubCommand("broadcast"))

        val suggestions = dispatcher.onSuggestions(sender, "tm", arrayOf("broadcast", "-f"))

        assertContentEquals(listOf("-fadein", "-fadeout"), suggestions.map { it.text })
        assertTrue(suggestions.all { it.tooltip != null })
    }

    private fun dispatcher(
        vararg subCommands: TitleManagerSubCommand,
        runTypes: MutableList<ConcurrencyType> = mutableListOf()
    ) = TitleManagerCommandDispatcher(
        PlaceholderConfiguration(locale = "da-DK"),
        runCommand = { concurrencyType, body ->
            runTypes += concurrencyType
            runBlocking {
                body()
            }
        },
        subCommands = subCommands.toList()
    )

    private fun senderCapturing(messages: MutableList<Component>, hasPermission: Boolean = true): CommandSender {
        val sender = mockk<CommandSender>(relaxed = true)

        every { sender.sendMessage(any<Component>()) } answers {
            messages += firstArg<Component>()
        }
        every { sender.hasPermission(any<String>()) } returns hasPermission

        return sender
    }

    private open class RecordingSubCommand(
        name: String,
        vararg aliases: String,
        concurrencyType: ConcurrencyType = ConcurrencyType.UNDEFINED,
        permission: String? = null,
        playerOnly: Boolean = false
    ) : TitleManagerSubCommand(name, *aliases, description = ReloadCommandMessages.description, concurrencyType = concurrencyType, permission = permission, playerOnly = playerOnly) {
        var lastCall: Call? = null

        override suspend fun executeCommand(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext) {
            lastCall = Call(args.toList().toTypedArray(), parameters, context)
        }
    }

    private class CompletingSubCommand(
        name: String,
        vararg aliases: String,
        private val completions: List<String>
    ) : RecordingSubCommand(name, *aliases) {
        var lastCompletion: CompletionCall? = null

        override fun tabComplete(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext): List<String> {
            lastCompletion = CompletionCall(args.toList().toTypedArray(), parameters, context)
            return completions
        }
    }

    private class RichCompletingSubCommand(
        name: String,
        vararg aliases: String
    ) : RecordingSubCommand(name, *aliases) {
        override fun suggest(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext) = listOf(
            dev.tarkan.titlemanager.bukkit.command.CommandSuggestion("broadcast", Component.text("Broadcast a title."))
        )
    }

    private data class Call(
        val args: Array<String>,
        val parameters: CommandParameters,
        val context: CommandContext
    )

    private data class CompletionCall(
        val args: Array<String>,
        val parameters: CommandParameters,
        val context: CommandContext
    )
}
