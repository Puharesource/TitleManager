package dev.tarkan.titlemanager.bukkit.command

internal object SafeModeCommandMessages {
    private const val UNKNOWN_CONFIGURATION_ERROR = "Unknown configuration error."

    fun version(pluginVersion: String): String = "TitleManager v$pluginVersion is running in safe mode."

    const val RELOAD_STARTED = "Reloading TitleManager from safe mode..."
    const val RELOAD_RECOVERED = "Reload finished. TitleManager is now running normally."
    const val RELOAD_FAILED = "Reload failed. TitleManager is still running in safe mode."
    const val CONFIGURATION_FAILED = "TitleManager is running in safe mode because configuration failed to load."
    const val HELP = "Available safe-mode commands: /tm version, /tm diagnostics, /tm reload"

    fun failureMessage(result: SafeModeReloadResult, failure: Throwable): String {
        return result.failureMessage ?: failure.message ?: UNKNOWN_CONFIGURATION_ERROR
    }

    fun failureMessage(failure: Throwable): String {
        return failure.message ?: UNKNOWN_CONFIGURATION_ERROR
    }
}
