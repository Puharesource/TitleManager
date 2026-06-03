import de.comahe.i18n4k.Locale
import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.command.CommandContext
import dev.tarkan.titlemanager.bukkit.command.CommandParameters
import dev.tarkan.titlemanager.bukkit.command.RootSubCommand
import dev.tarkan.titlemanager.bukkit.command.TitleManagerSubCommand
import dev.tarkan.titlemanager.bukkit.command.actionbar.ActionbarBroadcastSubCommand
import dev.tarkan.titlemanager.bukkit.command.actionbar.ActionbarMessageSubCommand
import dev.tarkan.titlemanager.bukkit.command.actionbar.ActionbarSubCommand
import dev.tarkan.titlemanager.bukkit.command.actionbar.WelcomeActionbarToggleSubCommand
import dev.tarkan.titlemanager.bukkit.command.playerlist.PlayerListSubCommand
import dev.tarkan.titlemanager.bukkit.command.playerlist.PlayerListToggleSubCommand
import dev.tarkan.titlemanager.bukkit.command.sidebar.SidebarSubCommand
import dev.tarkan.titlemanager.bukkit.command.sidebar.SidebarToggleSubCommand
import dev.tarkan.titlemanager.bukkit.command.title.TitleBroadcastSubCommand
import dev.tarkan.titlemanager.bukkit.command.title.TitleMessageSubCommand
import dev.tarkan.titlemanager.bukkit.command.title.TitleSubCommand
import dev.tarkan.titlemanager.bukkit.command.title.WelcomeTitleToggleSubCommand
import dev.tarkan.titlemanager.bukkit.concurrency.ConcurrencyType
import dev.tarkan.titlemanager.bukkit.concurrency.CoroutineScopeManager
import dev.tarkan.titlemanager.bukkit.context.PlayerContextManager
import dev.tarkan.titlemanager.bukkit.localization.ReloadCommandMessages
import dev.tarkan.titlemanager.bukkit.localization.TitleCommandMessages
import dev.tarkan.titlemanager.bukkit.storage.PlayerStorage
import dev.tarkan.titlemanager.bukkit.text.ComponentSerializer
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RootSubCommandTest {
    @Test
    fun `sends nested help when no child command is supplied`() = runTest {
        val messages = mutableListOf<Component>()
        val sender = senderCapturing(messages)
        val child = RecordingSubCommand("broadcast", "bc")
        val root = rootSubCommand(child)

        root.executeCommand(sender, emptyArray(), CommandParameters(emptyList()), commandContext())

        assertEquals(2, messages.size)
        assertTrue(ComponentSerializer.serialize(messages[0]).contains("/tm title"))
        assertTrue(ComponentSerializer.serialize(messages[1]).contains("/tm title broadcast"))
        assertNull(child.lastCall)
    }

    @Test
    fun `routes nested child commands by name case-insensitively`() = runTest {
        val child = RecordingSubCommand("broadcast", "bc")
        val root = rootSubCommand(child)
        val parameters = CommandParameters.fromArguments(listOf("-silent=true"))

        root.executeCommand(mockk(relaxed = true), arrayOf("BrOaDcAsT", "Hello"), parameters, commandContext())

        val call = child.lastCall!!
        assertContentEquals(arrayOf("Hello"), call.args)
        assertEquals("true", call.parameters.getValue("silent"))
    }

    @Test
    fun `routes nested child commands by alias case-insensitively`() = runTest {
        val child = RecordingSubCommand("broadcast", "bc")
        val root = rootSubCommand(child)

        root.executeCommand(mockk(relaxed = true), arrayOf("BC", "Hello"), CommandParameters(emptyList()), commandContext())

        assertContentEquals(arrayOf("Hello"), child.lastCall!!.args)
    }

    @Test
    fun `strips nested child parameters after child command name`() = runTest {
        val child = RecordingSubCommand("broadcast", "bc")
        val root = rootSubCommand(child)

        root.executeCommand(mockk(relaxed = true), arrayOf("broadcast", "-world=world_the_end", "Hello"), CommandParameters(emptyList()), commandContext())

        val call = child.lastCall!!
        assertEquals("world_the_end", call.parameters.getValue("world"))
        assertContentEquals(arrayOf("Hello"), call.args)
    }

    @Test
    fun `merges parent and nested child parameters`() = runTest {
        val child = RecordingSubCommand("broadcast", "bc")
        val root = rootSubCommand(child)

        root.executeCommand(
            mockk(relaxed = true),
            arrayOf("broadcast", "-radius=5", "Hello"),
            CommandParameters.fromArguments(listOf("-world=world")),
            commandContext()
        )

        val call = child.lastCall!!
        assertEquals("world", call.parameters.getValue("world"))
        assertEquals("5", call.parameters.getValue("radius"))
        assertContentEquals(arrayOf("Hello"), call.args)
    }

    @Test
    fun `uses runner when nested child has different concurrency`() = runTest {
        val runTypes = mutableListOf<ConcurrencyType>()
        val child = RecordingSubCommand("broadcast", concurrencyType = ConcurrencyType.ASYNC)
        val root = rootSubCommand(child, runTypes = runTypes)

        root.executeCommand(mockk(relaxed = true), arrayOf("broadcast"), CommandParameters(emptyList()), commandContext())

        assertEquals(ConcurrencyType.ASYNC, runTypes.single())
        assertContentEquals(emptyArray(), child.lastCall!!.args)
    }

    @Test
    fun `sends nested help for unknown child command`() = runTest {
        val messages = mutableListOf<Component>()
        val sender = senderCapturing(messages)
        val child = RecordingSubCommand("broadcast", "bc")
        val root = rootSubCommand(child)

        root.executeCommand(sender, arrayOf("missing"), CommandParameters(emptyList()), commandContext())

        assertEquals(2, messages.size)
        assertTrue(ComponentSerializer.serialize(messages[0]).contains("/tm title"))
        assertTrue(ComponentSerializer.serialize(messages[1]).contains("/tm title broadcast"))
        assertNull(child.lastCall)
    }

    @Test
    fun `denies nested child commands when sender lacks permission`() = runTest {
        val messages = mutableListOf<Component>()
        val sender = senderCapturing(messages, hasPermission = false)
        val child = RecordingSubCommand("broadcast", "bc", permission = "titlemanager.command.title.broadcast")
        val root = rootSubCommand(child)

        root.executeCommand(sender, arrayOf("broadcast"), CommandParameters(emptyList()), commandContext())

        assertEquals(1, messages.size)
        assertTrue(ComponentSerializer.serialize(messages.single()).contains("titlemanager.command.title.broadcast"))
        assertNull(child.lastCall)
    }

    @Test
    fun `denies nested player-only child commands for console senders`() = runTest {
        val messages = mutableListOf<Component>()
        val sender = senderCapturing(messages)
        val child = RecordingSubCommand("toggle", playerOnly = true)
        val root = rootSubCommand(child)

        root.executeCommand(sender, arrayOf("toggle"), CommandParameters(emptyList()), commandContext())

        assertEquals(1, messages.size)
        assertTrue(ComponentSerializer.serialize(messages.single()).contains("only be run as a player"))
        assertNull(child.lastCall)
    }

    @Test
    fun `hides nested child commands from help when sender lacks permission`() = runTest {
        val messages = mutableListOf<Component>()
        val sender = senderCapturing(messages, hasPermission = false)
        val visible = RecordingSubCommand("toggle")
        val hidden = RecordingSubCommand("broadcast", permission = "titlemanager.command.title.broadcast")
        val root = rootSubCommand(visible, hidden)

        root.executeCommand(sender, emptyArray(), CommandParameters(emptyList()), commandContext())

        assertEquals(2, messages.size)
        val helpText = messages.joinToString("\n") { ComponentSerializer.serialize(it) }
        assertTrue(helpText.contains("/tm title toggle"))
        assertFalse(helpText.contains("/tm title broadcast"))
    }

    @Test
    fun `tab completes visible child commands and aliases by prefix`() {
        val sender = senderCapturing(mutableListOf())
        val root = rootSubCommand(
            RecordingSubCommand("broadcast", "bc"),
            RecordingSubCommand("toggle", permission = "titlemanager.command.title.toggle")
        )

        assertContentEquals(
            listOf("bc", "broadcast"),
            root.tabComplete(sender, arrayOf("b"), CommandParameters(emptyList()), commandContext())
        )
    }

    @Test
    fun `tab completion hides nested child commands when sender lacks permission`() {
        val sender = senderCapturing(mutableListOf(), hasPermission = false)
        val root = rootSubCommand(
            RecordingSubCommand("broadcast", "bc", permission = "titlemanager.command.title.broadcast"),
            RecordingSubCommand("toggle")
        )

        assertContentEquals(
            listOf("toggle"),
            root.tabComplete(sender, arrayOf(""), CommandParameters(emptyList()), commandContext())
        )
    }

    @Test
    fun `tab completion delegates args and merged parameters to matched child command`() {
        val child = CompletingSubCommand("broadcast", "bc", completions = listOf("Hello"))
        val root = rootSubCommand(child)

        val completions = root.tabComplete(
            mockk(relaxed = true),
            arrayOf("broadcast", "-radius=5", "H"),
            CommandParameters.fromArguments(listOf("-world=world")),
            commandContext()
        )

        assertContentEquals(listOf("Hello"), completions)
        assertContentEquals(arrayOf("H"), child.lastCompletion!!.args)
        assertEquals("world", child.lastCompletion!!.parameters.getValue("world"))
        assertEquals("5", child.lastCompletion!!.parameters.getValue("radius"))
    }

    @Test
    fun `concrete root command groups expose expected legacy aliases and children`() {
        val coroutineScopeManager = mockk<CoroutineScopeManager>(relaxed = true)
        val playerStorage = mockk<PlayerStorage>(relaxed = true)
        val playerContextManager = mockk<PlayerContextManager>(relaxed = true)
        val plugin = mockk<TitleManagerPlugin>(relaxed = true)

        val title = TitleSubCommand(
            coroutineScopeManager,
            TitleBroadcastSubCommand(plugin, playerContextManager),
            TitleMessageSubCommand(plugin, playerContextManager),
            WelcomeTitleToggleSubCommand(playerStorage)
        )
        val actionbar = ActionbarSubCommand(
            coroutineScopeManager,
            ActionbarBroadcastSubCommand(plugin, playerContextManager),
            ActionbarMessageSubCommand(plugin, playerContextManager),
            WelcomeActionbarToggleSubCommand(playerStorage)
        )
        val sidebar = SidebarSubCommand(
            coroutineScopeManager,
            SidebarToggleSubCommand(playerContextManager, playerStorage)
        )
        val playerList = PlayerListSubCommand(
            coroutineScopeManager,
            PlayerListToggleSubCommand(playerContextManager, playerStorage)
        )

        assertEquals("title", title.name)
        assertEquals(listOf("t"), title.aliases.toList())
        assertEquals(listOf("broadcast", "msg", "toggle"), title.subCommands.map { it.name })

        assertEquals("actionbar", actionbar.name)
        assertEquals(listOf("a"), actionbar.aliases.toList())
        assertEquals(listOf("bc", "msg", "toggle"), actionbar.subCommands.map { it.name })

        assertEquals("sidebar", sidebar.name)
        assertEquals(listOf("sb", "scoreboard"), sidebar.aliases.toList())
        assertEquals(listOf("toggle"), sidebar.subCommands.map { it.name })

        assertEquals("playerlist", playerList.name)
        assertEquals(listOf("pl"), playerList.aliases.toList())
        assertEquals(listOf("toggle"), playerList.subCommands.map { it.name })
    }

    private fun rootSubCommand(
        vararg child: TitleManagerSubCommand,
        runTypes: MutableList<ConcurrencyType> = mutableListOf()
    ) = object : RootSubCommand(
        "title",
        "t",
        description = TitleCommandMessages.description,
        runCommand = { concurrencyType, body ->
            runTypes += concurrencyType
            runBlocking {
                body()
            }
        },
        subCommands = child.toList()
    ) {}

    private fun senderCapturing(messages: MutableList<Component>, hasPermission: Boolean = true): CommandSender {
        val sender = mockk<CommandSender>(relaxed = true)

        every { sender.sendMessage(any<Component>()) } answers {
            messages += firstArg<Component>()
        }
        every { sender.sendMessage(any<String>()) } answers {
            messages += ComponentSerializer.deserialize(firstArg<String>())
        }
        every { sender.hasPermission(any<String>()) } returns hasPermission

        return sender
    }

    private fun commandContext() = CommandContext(Locale.forLanguageTag("en-US"))

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
