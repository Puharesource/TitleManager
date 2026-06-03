package dev.tarkan.titlemanager.bukkit.diagnostics

data class DiagnosticsSnapshot(
    val mode: DiagnosticsMode,
    val pluginVersion: String,
    val serverName: String,
    val serverVersion: String,
    val bukkitVersion: String,
    val versionModule: String,
    val versionModuleThreading: String,
    val schedulerMode: String,
    val loadedAnimationFiles: Int,
    val registeredAnimationPlaceholders: Int,
    val capabilities: List<DiagnosticsStatus>,
    val integrations: List<DiagnosticsStatus>,
    val validationErrors: List<String>
)

enum class DiagnosticsMode(val displayName: String) {
    NORMAL("normal"),
    SAFE_MODE("safe-mode")
}
