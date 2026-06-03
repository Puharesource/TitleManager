import dev.tarkan.titlemanager.bukkit.command.SafeModeTitleManagerCommand
import dev.tarkan.titlemanager.bukkit.command.SafeModeReloadResult
import dev.tarkan.titlemanager.bukkit.configuration.ConfigurationException
import dev.tarkan.titlemanager.bukkit.diagnostics.DiagnosticsMode
import dev.tarkan.titlemanager.bukkit.diagnostics.DiagnosticsSnapshot
import dev.tarkan.titlemanager.bukkit.diagnostics.DiagnosticsStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.command.CommandSender
import kotlin.test.Test
import kotlin.test.assertTrue

class SafeModeTitleManagerCommandTest {
    @Test
    fun `safe mode help reports configuration failure and available commands`() {
        val sender = mockk<CommandSender>(relaxed = true)
        val command = safeModeCommand()

        assertTrue(command.onCommand(sender, mockk(), "tm", emptyArray()))

        verify {
            sender.sendMessage("TitleManager is running in safe mode because configuration failed to load.")
            sender.sendMessage("Broken config")
            sender.sendMessage("Available safe-mode commands: /tm version, /tm diagnostics, /tm reload")
        }
    }

    @Test
    fun `safe mode diagnostics reports inactive runtime and validation error`() {
        val messages = mutableListOf<String>()
        val sender = senderCapturing(messages)
        val command = safeModeCommand()

        assertTrue(command.onCommand(sender, mockk(), "tm", arrayOf("diagnostics")))

        assertTrue(messages.contains("TitleManager diagnostics"))
        assertTrue(messages.contains("Mode: safe-mode"))
        assertTrue(messages.contains("Scheduler: inactive (safe mode)"))
        assertTrue(messages.contains("Registered animation placeholders: 0"))
        assertTrue(messages.contains("- Broken config"))
    }

    @Test
    fun `safe mode version command does not require loaded configuration`() {
        val sender = mockk<CommandSender>(relaxed = true)
        val command = safeModeCommand(version = "3.0.0-SNAPSHOT")

        assertTrue(command.onCommand(sender, mockk(), "tm", arrayOf("version")))

        verify {
            sender.sendMessage("TitleManager v3.0.0-SNAPSHOT is running in safe mode.")
        }
    }

    @Test
    fun `safe mode reload delegates to plugin reload path`() {
        val sender = mockk<CommandSender>(relaxed = true)
        val reload = mockk<() -> SafeModeReloadResult>()
        every { reload.invoke() } returns SafeModeReloadResult(recovered = true)
        val command = safeModeCommand(reload = reload)

        assertTrue(command.onCommand(sender, mockk(), "tm", arrayOf("reload")))

        verify {
            sender.sendMessage("Reloading TitleManager from safe mode...")
            reload.invoke()
            sender.sendMessage("Reload finished. TitleManager is now running normally.")
        }
    }

    @Test
    fun `safe mode reload reports failure when runtime is still not recovered`() {
        val sender = mockk<CommandSender>(relaxed = true)
        val reload = mockk<() -> SafeModeReloadResult>()
        every { reload.invoke() } returns SafeModeReloadResult(
            recovered = false,
            failureMessage = "Still broken"
        )
        val command = safeModeCommand(reload = reload)

        assertTrue(command.onCommand(sender, mockk(), "tm", arrayOf("reload")))

        verify {
            sender.sendMessage("Reloading TitleManager from safe mode...")
            reload.invoke()
            sender.sendMessage("Reload failed. TitleManager is still running in safe mode.")
            sender.sendMessage("Still broken")
        }
    }

    private fun safeModeCommand(
        version: String = "test",
        failure: ConfigurationException = ConfigurationException("Broken config"),
        reload: () -> SafeModeReloadResult = { SafeModeReloadResult(recovered = true) }
    ) = SafeModeTitleManagerCommand(version, failure, reload) {
        DiagnosticsSnapshot(
            mode = DiagnosticsMode.SAFE_MODE,
            pluginVersion = version,
            serverName = "MockBukkit",
            serverVersion = "test",
            bukkitVersion = "test",
            versionModule = "unavailable (runtime not started)",
            versionModuleThreading = "inactive",
            schedulerMode = "inactive (safe mode)",
            loadedAnimationFiles = 0,
            registeredAnimationPlaceholders = 0,
            capabilities = listOf(DiagnosticsStatus("runtime-features", "unavailable", "configuration failed before runtime startup")),
            integrations = emptyList(),
            validationErrors = listOf(failure.message ?: "Unknown configuration error.")
        )
    }

    private fun senderCapturing(messages: MutableList<String>): CommandSender {
        val sender = mockk<CommandSender>(relaxed = true)

        every { sender.sendMessage(any<String>()) } answers {
            messages += firstArg<String>()
        }

        return sender
    }
}
