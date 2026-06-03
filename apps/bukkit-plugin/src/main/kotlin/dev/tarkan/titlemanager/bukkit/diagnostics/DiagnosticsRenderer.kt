package dev.tarkan.titlemanager.bukkit.diagnostics

object DiagnosticsRenderer {
    fun render(snapshot: DiagnosticsSnapshot): List<String> = buildList {
        add("TitleManager diagnostics")
        add("Mode: ${snapshot.mode.displayName}")
        add("Plugin version: ${snapshot.pluginVersion}")
        add("Server: ${snapshot.serverName} ${snapshot.serverVersion}")
        add("Bukkit API: ${snapshot.bukkitVersion}")
        add("Version module: ${snapshot.versionModule}")
        add("Version module threading: ${snapshot.versionModuleThreading}")
        add("Scheduler: ${snapshot.schedulerMode}")
        add("Loaded animation files: ${snapshot.loadedAnimationFiles}")
        add("Registered animation placeholders: ${snapshot.registeredAnimationPlaceholders}")
        add("Capabilities: ${snapshot.capabilities.joinToString(", ") { it.render() }}")
        add("Integrations: ${snapshot.integrations.joinToString(", ") { it.render() }}")

        if (snapshot.validationErrors.isEmpty()) {
            add("Validation errors: none")
        } else {
            add("Validation errors:")
            snapshot.validationErrors.forEach { validationError ->
                add("- $validationError")
            }
        }
    }
}
