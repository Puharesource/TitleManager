package dev.tarkan.titlemanager.bukkit.plugin

import dev.tarkan.titlemanager.api.TitleManagerCoreApi
import dev.tarkan.titlemanager.bukkit.announcer.AnnouncerService
import dev.tarkan.titlemanager.bukkit.animation.ClampedColorGradientAnimation
import dev.tarkan.titlemanager.bukkit.animation.ColorCycleAnimation
import dev.tarkan.titlemanager.bukkit.animation.ColorGradientAnimation
import dev.tarkan.titlemanager.bukkit.animation.DefaultAnimationFiles
import dev.tarkan.titlemanager.bukkit.animation.addAnimationFilePlaceholders
import dev.tarkan.titlemanager.bukkit.api.DefaultTitleManagerApi
import dev.tarkan.titlemanager.bukkit.api.TitleManagerApi
import dev.tarkan.titlemanager.bukkit.command.SafeModeReloadResult
import dev.tarkan.titlemanager.bukkit.command.SafeModeTitleManagerCommand
import dev.tarkan.titlemanager.bukkit.command.TitleManagerCommand
import dev.tarkan.titlemanager.bukkit.command.actionbar.ActionbarBroadcastSubCommand
import dev.tarkan.titlemanager.bukkit.command.actionbar.ActionbarMessageSubCommand
import dev.tarkan.titlemanager.bukkit.command.actionbar.ActionbarSubCommand
import dev.tarkan.titlemanager.bukkit.command.actionbar.WelcomeActionbarToggleSubCommand
import dev.tarkan.titlemanager.bukkit.command.playerlist.PlayerListSubCommand
import dev.tarkan.titlemanager.bukkit.command.playerlist.PlayerListToggleSubCommand
import dev.tarkan.titlemanager.bukkit.command.sidebar.SidebarSubCommand
import dev.tarkan.titlemanager.bukkit.command.sidebar.SidebarToggleSubCommand
import dev.tarkan.titlemanager.bukkit.command.subcommands.*
import dev.tarkan.titlemanager.bukkit.command.title.TitleBroadcastSubCommand
import dev.tarkan.titlemanager.bukkit.command.title.TitleMessageSubCommand
import dev.tarkan.titlemanager.bukkit.command.title.TitleSubCommand
import dev.tarkan.titlemanager.bukkit.command.title.WelcomeTitleToggleSubCommand
import dev.tarkan.titlemanager.bukkit.concurrency.CoroutineScopeManager
import dev.tarkan.titlemanager.bukkit.configuration.*
import dev.tarkan.titlemanager.bukkit.context.PlayerContext
import dev.tarkan.titlemanager.bukkit.context.PlayerContextManager
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeAnimationDiagnostics
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapabilities
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapabilityRegistry
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeDiagnosticsService
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeVersionModule
import dev.tarkan.titlemanager.bukkit.diagnostics.VersionModuleSelector
import dev.tarkan.titlemanager.bukkit.extensions.getFormattedTime
import dev.tarkan.titlemanager.bukkit.integration.BungeeCordService
import dev.tarkan.titlemanager.bukkit.integration.ExternalPlaceholderIntegration
import dev.tarkan.titlemanager.bukkit.integration.VaultIntegration
import dev.tarkan.titlemanager.bukkit.lifecycle.TitleManagerReloader
import dev.tarkan.titlemanager.bukkit.lifecycle.TransactionalTitleManagerReloader
import dev.tarkan.titlemanager.bukkit.listeners.*
import dev.tarkan.titlemanager.bukkit.metrics.MetricsService
import dev.tarkan.titlemanager.bukkit.storage.PlayerStorage
import dev.tarkan.titlemanager.bukkit.storage.SqlitePlayerStorage
import dev.tarkan.titlemanager.bukkit.text.ComponentSerializer
import dev.tarkan.titlemanager.bukkit.update.GitHubReleaseUpdateClient
import dev.tarkan.titlemanager.bukkit.update.UpdateClient
import dev.tarkan.titlemanager.bukkit.update.UpdateService
import dev.tarkan.titlemanager.parser.IntermediaryParser
import dev.tarkan.titlemanager.parser.animation.AnimationParser
import dev.tarkan.titlemanager.parser.placeholder.animation.AnimationPlaceholderRegistry
import dev.tarkan.titlemanager.parser.placeholder.animation.addCoreAnimationPlaceholders
import dev.tarkan.titlemanager.parser.placeholder.variable.VariablePlaceholderRegistry
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.ServicePriority
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.util.*
import kotlin.io.path.*
import kotlin.math.roundToInt

open class TitleManagerPlugin : JavaPlugin() {
    companion object {
        internal var versionModuleSelectorFactory: () -> VersionModuleSelector = { VersionModuleSelector.fromServiceLoader() }
        internal var updateClientFactory: () -> UpdateClient = { GitHubReleaseUpdateClient() }
        private const val VAULT_ECONOMY_CLASS = "net.milkbowl.vault.economy.Economy"
        private const val VAULT_PERMISSION_CLASS = "net.milkbowl.vault.permission.Permission"
        private const val LEGACY_GOLD = "§6"
        private const val LEGACY_GREEN = "§a"
        private const val LEGACY_YELLOW = "§e"
        private const val LEGACY_RED = "§c"
    }


    private lateinit var koin: KoinApplication
    private var koinStarted = false
    private var configurationFailure: ConfigurationException? = null
    private var configurationFailureRuntimeCapabilities: RuntimeCapabilities? = null

    override fun onEnable() {
        try {
            enablePlugin()
        } catch (exception: ConfigurationException) {
            enterSafeMode(exception)
        }
    }

    val pluginVersion: String
        get() = pluginMetaVersion() ?: pluginDescriptionVersion()

    private fun enablePlugin() {
        configurationFailureRuntimeCapabilities = null
        val configurationManager = ConfigurationManager(this, dataFolder.toPath())
        val versionModuleSelector = createVersionModuleSelector()
        val runtimeVersionModule = versionModuleSelector.select(server)
        val runtimeCapabilityRegistry = RuntimeCapabilityRegistry(runtimeVersionModule)
        val runtimeCapabilities = runtimeCapabilityRegistry.normalRuntime(server)
        configurationFailureRuntimeCapabilities = runtimeCapabilities
        val isVaultEconomyAvailable = isServicePresent(VAULT_ECONOMY_CLASS)
        val isVaultPermissionGroupsAvailable = hasVaultPermissionGroupSupport()

        RuntimeConfigurationValidator(
            vaultEconomyAvailable = isVaultEconomyAvailable,
            vaultPermissionGroupsAvailable = isVaultPermissionGroupsAvailable
        ).validate(configurationManager, runtimeCapabilities)

        val animationsFolder = DefaultAnimationFiles.copyMissingTo(dataFolder.toPath(), ::getResource)

        val animationFiles = animationsFolder
            .listDirectoryEntries("*.txt")
            .filter { !it.isDirectory() }

        Class.forName("org.sqlite.JDBC")

        koin = startKoin {
            modules(
                module {
                    single<TitleManagerPlugin> { this@TitleManagerPlugin }
                    single<TitleManagerReloader> { createReloader() }
                    single<TitleManagerApi> { DefaultTitleManagerApi(get()) }

                    single<ConfigurationManager>(createdAtStart = true) { configurationManager }
                    single<AdvancedConfiguration>(createdAtStart = true) { configurationManager.advancedConfiguration }
                    single<PlayerListConfiguration>(createdAtStart = true) { configurationManager.playerListConfiguration }
                    single<PlaceholderConfiguration>(createdAtStart = true) { configurationManager.placeholderConfiguration }
                    single<WelcomeTitleConfiguration>(createdAtStart = true) { configurationManager.welcomeTitleConfiguration }
                    single<WelcomeActionbarConfiguration>(createdAtStart = true) { configurationManager.welcomeActionbarConfiguration }
                    single<ScoreboardConfiguration>(createdAtStart = true) { configurationManager.scoreboardConfiguration }
                    single<GradientsConfiguration>(createdAtStart = true) { configurationManager.gradientsConfiguration }
                    single<AnnouncerConfiguration>(createdAtStart = true) { configurationManager.announcerConfiguration }
                    single<HooksConfiguration>(createdAtStart = true) { configurationManager.hooksConfiguration }

                    single<PlayerStorage>(createdAtStart = true) { SqlitePlayerStorage(this@TitleManagerPlugin) }

                    singleOf(::CoroutineScopeManager)
                    singleOf(::PlayerContextManager)
                    single<IntermediaryParser>(createdAtStart = true) { IntermediaryParser(50u) }

                    single<VariablePlaceholderRegistry<PlayerContext>>(createdAtStart = true) {
                        VariablePlaceholderRegistry.build {
                            addWithContextNoData("max-players", "max") {
                                it.plugin.server.maxPlayers.toString()
                            }
                            addWithContext("online-players", "online") { data, context ->
                                if (data.isNullOrBlank() || !configurationManager.advancedConfiguration.usingBungeeCord) {
                                    context.plugin.server.onlinePlayers.size.toString()
                                } else {
                                    koin.get<BungeeCordService>().playerCount(data)
                                }
                            }

                            addWithContextNoData("safe-online", "safe-online-players") {
                                it.plugin.server.onlinePlayers.count { onlinePlayer -> it.player.canSee(onlinePlayer) }.toString()
                            }

                            addWithContextNoData("bungeecord-online", "bungeecord-online-players", cacheTime = 5u) {
                                koin.get<BungeeCordService>().onlinePlayers.toString()
                            }

                            addWithContextNoData("server", "server-name") {
                                koin.get<BungeeCordService>().currentServer.orEmpty()
                            }

                            addWithContextNoData("balance", "money") {
                                if (isVaultEconomyAvailable) {
                                    koin.get<VaultIntegration>().balance(it.player)
                                } else {
                                    "no-econ"
                                }
                            }

                            addWithContextNoData("group", "group-name") {
                                if (isVaultPermissionGroupsAvailable) {
                                    koin.get<VaultIntegration>().group(it.player)
                                } else {
                                    "no-perms"
                                }
                            }

                            addWithContextNoData("world-online", "world-players") {
                                it.player.world.playerCount.toString()
                            }

                            addWithContextNoData("world", "world-name") {
                                it.player.world.name
                            }

                            addWithContextNoData("world-time") {
                                it.player.world.time.toString()
                            }

                            addWithContextNoData("server-time") {
                                configurationManager.placeholderConfiguration.simpleDateFormat.format(Date(System.currentTimeMillis()))
                            }

                            addWithContextNoData("24h-world-time") {
                                it.player.world.getFormattedTime(true)
                            }

                            addWithContextNoData("12h-world-time") {
                                it.player.world.getFormattedTime(false)
                            }

                            addWithContextNoData("name", "player", "username") {
                                it.player.name
                            }

                            addWithContextNoData("displayname", "display-name", "nickname", "nick") {
                                ComponentSerializer.serialize(it.player.displayName())
                            }

                            addWithContextNoData("strippeddisplayname", "stripped-displayname", "stripped-nickname", "stripped-nick") {
                                PlainTextComponentSerializer.plainText().serialize(it.player.displayName())
                            }

                            addWithContextNoData("ping") {
                                it.player.ping.toString()
                            }

                            addWithContext("tps", cacheTime = 30u) { data, context ->
                                context.plugin.server.tps.formatTps(data)
                            }

                            addSimple("color", "colour", "c") { data ->
                                require(!data.isNullOrBlank()) { "No legacy color value found" }

                                TitleManagerCoreApi.legacyColorCode(data)
                            }

                            addSimple("gradient") { data ->
                                TitleManagerCoreApi.formatLegacyGradient(data)
                            }

                            configurationManager.placeholderConfiguration.aliases.forEach { (alias, value) ->
                                addSimple(alias) { value }
                            }
                        }
                    }

                    single<AnimationPlaceholderRegistry<PlayerContext>>(createdAtStart = true) {
                        AnimationPlaceholderRegistry.build {
                            addCoreAnimationPlaceholders()

                            addSimple("cycle", dataSerializer = ColorCycleAnimation.DataSerializer(configurationManager.gradientsConfiguration)) { ColorCycleAnimation(it?.timing, it!!.gradient) }

                            addSimple("gradient", dataSerializer = ColorGradientAnimation.DataSerializer(configurationManager.gradientsConfiguration)) { ColorGradientAnimation(it?.timing, it!!.gradient, it.separator, it.text) }
                            addSimple("cgradient", dataSerializer = ClampedColorGradientAnimation.DataSerializer(configurationManager.gradientsConfiguration)) { ClampedColorGradientAnimation(it?.timing, it!!.gradient, it.separator, it.text) }

                            addAnimationFilePlaceholders(
                                animationFiles,
                                koin.get<IntermediaryParser>()
                            ) {
                                koin.get<AnimationParser<PlayerContext>>()
                            }
                        }
                    }

                    single<AnimationParser<PlayerContext>>(createdAtStart = true) { AnimationParser(get(), get()) }
                    single { RuntimeAnimationDiagnostics(animationFiles.size) }
                    single<VersionModuleSelector> { versionModuleSelector }
                    single<RuntimeVersionModule> { runtimeVersionModule }
                    single<RuntimeCapabilityRegistry> { runtimeCapabilityRegistry }
                    single<UpdateClient> { updateClientFactory() }
                    singleOf(::UpdateService)
                    singleOf(::RuntimeDiagnosticsService)
                    singleOf(::MetricsService)

                    singleOf(::TitleSubCommand)
                    singleOf(::ActionbarSubCommand)
                    singleOf(::SidebarSubCommand)
                    singleOf(::PlayerListSubCommand)
                    singleOf(::TitleBroadcastSubCommand)
                    singleOf(::TitleMessageSubCommand)
                    singleOf(::ActionbarBroadcastSubCommand)
                    singleOf(::ListAnimationsSubCommand)
                    singleOf(::ListScriptsSubCommand)
                    singleOf(::ActionbarMessageSubCommand)
                    singleOf(::DiagnosticsSubCommand)
                    singleOf(::ReloadSubCommand)
                    singleOf(::VersionSubCommand)
                    singleOf(::SidebarToggleSubCommand)
                    singleOf(::PlayerListToggleSubCommand)
                    singleOf(::WelcomeTitleToggleSubCommand)
                    singleOf(::WelcomeActionbarToggleSubCommand)
                    singleOf(::TitleManagerCommand)
                    single { ExternalPlaceholderIntegration(this@TitleManagerPlugin) }
                    single { BungeeCordService(this@TitleManagerPlugin) }
                    singleOf(::AnnouncerService)
                    if (isVaultEconomyAvailable || isVaultPermissionGroupsAvailable) {
                        single { VaultIntegration(this@TitleManagerPlugin, configurationManager.placeholderConfiguration) }
                    }

                    singleOf(::PlayerContextListener)
                    singleOf(::PlayerListFeatureListener)
                    singleOf(::WelcomeTitleFeatureListener)
                    singleOf(::WelcomeActionbarFeatureListener)
                    singleOf(::ScoreboardFeatureListener)
                    singleOf(::UpdateNotificationListener)
                }
            )
        }

        val advancedConfiguration = koin.koin.get<AdvancedConfiguration>()
        if (advancedConfiguration.debug) {
            logger.info(
                "Debug mode enabled: runtimeModule=${runtimeVersionModule.id}, " +
                    "usingConfig=${advancedConfiguration.usingConfig}, " +
                    "usingBungeeCord=${advancedConfiguration.usingBungeeCord}"
            )
        }

        server.pluginManager.registerEvents(koin.koin.get<PlayerContextListener>(), this)

        if (advancedConfiguration.usingConfig) {
            server.pluginManager.registerEvents(koin.koin.get<PlayerListFeatureListener>(), this)
            server.pluginManager.registerEvents(koin.koin.get<WelcomeTitleFeatureListener>(), this)
            server.pluginManager.registerEvents(koin.koin.get<WelcomeActionbarFeatureListener>(), this)
            server.pluginManager.registerEvents(koin.koin.get<ScoreboardFeatureListener>(), this)
            koin.koin.get<AnnouncerService>().start()
        }

        if (advancedConfiguration.usingBungeeCord) {
            koin.koin.get<BungeeCordService>().start()
        }
        koin.koin.get<UpdateService>().start()
        koin.koin.get<MetricsService>()
        server.pluginManager.registerEvents(koin.koin.get<UpdateNotificationListener>(), this)

        val playerStorage = koin.koin.get<PlayerStorage>()
        val playerContextManager = koin.koin.get<PlayerContextManager>()

        runBlocking {
            for (player in server.onlinePlayers) {
                val playerInfo = playerStorage.load(player.uniqueId)
                val context = playerContextManager.getContext(player)

                if (advancedConfiguration.usingConfig && playerInfo.isPlayerListEnabled) {
                    context.setConfigPlayerList()
                }

                if (advancedConfiguration.usingConfig && playerInfo.isSidebarEnabled) {
                    context.setConfigScoreboard()
                }
            }
        }

        val command = koin.koin.get<TitleManagerCommand>()
        getCommand("titlemanager")?.setExecutor(command)
        getCommand("titlemanager")?.setTabCompleter(command)
        server.servicesManager.register(TitleManagerApi::class.java, koin.koin.get(), this, ServicePriority.Normal)
        configurationFailure = null
        configurationFailureRuntimeCapabilities = null
        koinStarted = true
    }

    private fun enterSafeMode(exception: ConfigurationException) {
        koinStarted = false
        configurationFailure = exception
        logger.severe("TitleManager started in safe mode: ${exception.message}")
        getCommand("titlemanager")?.setExecutor(
            SafeModeTitleManagerCommand(
                pluginVersion,
                exception,
                reload = {
                    reload()
                    SafeModeReloadResult(
                        recovered = koinStarted,
                        failureMessage = configurationFailure?.message
                    )
                },
                diagnosticsSnapshot = {
                    RuntimeDiagnosticsService.safeModeSnapshot(
                        pluginVersion = pluginVersion,
                        server = server,
                        validationError = configurationFailure?.message ?: exception.message ?: "Unknown configuration error.",
                        runtimeCapabilities = configurationFailureRuntimeCapabilities ?: RuntimeCapabilityRegistry().safeModeRuntime()
                    )
                }
            )
        )
    }

    override fun onDisable() {
        if (!koinStarted) {
            return
        }

        try {
            server.servicesManager.unregisterAll(this)
            closeRuntimeResources()
        } finally {
            stopKoin()
            koinStarted = false
        }
    }

    fun reload() {
        createReloader().reload()
    }

    protected open fun createVersionModuleSelector(): VersionModuleSelector = versionModuleSelectorFactory()

    private fun pluginMetaVersion(): String? {
        val pluginMeta = runCatching { javaClass.getMethod("getPluginMeta").invoke(this) }.getOrNull() ?: return null
        return runCatching { pluginMeta.javaClass.getMethod("getVersion").invoke(pluginMeta) as? String }.getOrNull()
    }

    @Suppress("DEPRECATION")
    private fun pluginDescriptionVersion(): String = description.version

    @Suppress("UNCHECKED_CAST")
    private fun isServicePresent(className: String): Boolean {
        return try {
            val serviceClass = Class.forName(className, false, javaClass.classLoader) as Class<Any>
            server.servicesManager.getRegistration(serviceClass) != null
        } catch (_: ClassNotFoundException) {
            false
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun hasVaultPermissionGroupSupport(): Boolean {
        return try {
            val serviceClass = Class.forName(VAULT_PERMISSION_CLASS, false, javaClass.classLoader) as Class<Any>
            val provider = server.servicesManager.getRegistration(serviceClass)?.provider ?: return false
            provider.javaClass.getMethod("hasGroupSupport").invoke(provider) == true
        } catch (_: ClassNotFoundException) {
            false
        } catch (_: ReflectiveOperationException) {
            false
        }
    }

    private fun createReloader() = TransactionalTitleManagerReloader(
        isRuntimeStarted = { koinStarted },
        validateConfiguration = { ConfigurationManager(this, dataFolder.toPath()) },
        disableRuntime = { onDisable() },
        enableRuntime = { onEnable() }
    )

    private fun closeRuntimeResources() {
        val advancedConfiguration = koin.koin.get<AdvancedConfiguration>()

        koin.koin.get<PlayerContextListener>().close()
        koin.koin.get<UpdateNotificationListener>().close()

        if (advancedConfiguration.usingConfig) {
            koin.koin.get<PlayerListFeatureListener>().close()
            koin.koin.get<WelcomeTitleFeatureListener>().close()
            koin.koin.get<WelcomeActionbarFeatureListener>().close()
            koin.koin.get<ScoreboardFeatureListener>().close()
            koin.koin.get<AnnouncerService>().close()
        }

        koin.koin.get<BungeeCordService>().close()
        koin.koin.get<PlayerContextManager>().close()
        koin.koin.get<RuntimeVersionModule>().close()
        koin.koin.get<CoroutineScopeManager>().close()
        koin.koin.get<PlayerStorage>().close()
    }

    private fun DoubleArray.formatTps(data: String?): String {
        val oneMinute = getOrElse(0) { 20.0 }
        val fiveMinute = getOrElse(1) { oneMinute }
        val fifteenMinute = getOrElse(2) { fiveMinute }
        val requestedMinutes = data?.toIntOrNull()

        return when {
            data == null || requestedMinutes == 1 -> oneMinute.formatTpsNumber()
            requestedMinutes == 5 -> fiveMinute.formatTpsNumber()
            requestedMinutes == 15 -> fifteenMinute.formatTpsNumber()
            requestedMinutes != null || data.equals("short", ignoreCase = true) -> formatTpsList(oneMinute, fiveMinute, fifteenMinute)
            else -> "$LEGACY_GOLD TPS from last 1m, 5m, 15m: ${formatTpsList(oneMinute, fiveMinute, fifteenMinute)}"
        }
    }

    private fun formatTpsList(oneMinute: Double, fiveMinute: Double, fifteenMinute: Double): String {
        return "${oneMinute.formatTpsNumber(withStar = true)}, ${fiveMinute.formatTpsNumber(withStar = true)}, ${fifteenMinute.formatTpsNumber(withStar = true)}"
    }

    private fun Double.formatTpsNumber(withStar: Boolean = false): String {
        val color = when {
            this > 18.0 -> LEGACY_GREEN
            this > 16.0 -> LEGACY_YELLOW
            else -> LEGACY_RED
        }
        val star = if (withStar && this > 20.0) "*" else ""
        val roundedTps = ((this * 100.0).roundToInt() / 100.0).coerceAtMost(20.0)

        return "$color$star$roundedTps"
    }

}