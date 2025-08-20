package studio.minekarta.titlemanagerreborn.internal.modules

import dagger.Module
import dagger.Provides
import studio.minekarta.titlemanagerreborn.TitleManagerReborn
import studio.minekarta.titlemanagerreborn.internal.config.TMConfigMain
import studio.minekarta.titlemanagerreborn.internal.model.script.BuiltinScripts
import studio.minekarta.titlemanagerreborn.internal.reflections.NMSManager
import studio.minekarta.titlemanagerreborn.internal.services.TitleManagerService
import studio.minekarta.titlemanagerreborn.internal.services.TitleManagerServiceSpigot
import studio.minekarta.titlemanagerreborn.internal.services.animation.AnimationsService
import studio.minekarta.titlemanagerreborn.internal.services.animation.AnimationsServiceFile
import studio.minekarta.titlemanagerreborn.internal.services.animation.ScriptService
import studio.minekarta.titlemanagerreborn.internal.services.animation.ScriptServiceNashorn
import studio.minekarta.titlemanagerreborn.internal.services.animation.ScriptServiceNotFound
import studio.minekarta.titlemanagerreborn.internal.services.announcer.AnnouncerService
import studio.minekarta.titlemanagerreborn.internal.services.announcer.AnnouncerServiceSpigot
import studio.minekarta.titlemanagerreborn.internal.services.bungeecord.BungeeCordService
import studio.minekarta.titlemanagerreborn.internal.services.bungeecord.BungeeCordServiceSpigot
import studio.minekarta.titlemanagerreborn.internal.services.event.ListenerService
import studio.minekarta.titlemanagerreborn.internal.services.event.ListenerServiceSpigot
import studio.minekarta.titlemanagerreborn.internal.services.features.ActionbarService
import studio.minekarta.titlemanagerreborn.internal.services.features.ActionbarServiceSpigot
import studio.minekarta.titlemanagerreborn.internal.services.features.PlayerListService
import studio.minekarta.titlemanagerreborn.internal.services.features.PlayerListServiceSpigot
import studio.minekarta.titlemanagerreborn.internal.services.features.ScoreboardService
import studio.minekarta.titlemanagerreborn.internal.services.features.ScoreboardServiceSpigotNms
import studio.minekarta.titlemanagerreborn.internal.services.features.ScoreboardServiceSpigot
import studio.minekarta.titlemanagerreborn.internal.services.features.TitleService
import studio.minekarta.titlemanagerreborn.internal.services.features.TitleServiceSpigot
import studio.minekarta.titlemanagerreborn.internal.services.placeholder.PlaceholderService
import studio.minekarta.titlemanagerreborn.internal.services.placeholder.PlaceholderServiceText
import studio.minekarta.titlemanagerreborn.internal.services.storage.PlayerInfoService
import studio.minekarta.titlemanagerreborn.internal.services.storage.PlayerInfoServiceSqlite
import studio.minekarta.titlemanagerreborn.internal.services.task.SchedulerService
import studio.minekarta.titlemanagerreborn.internal.services.task.SchedulerServiceAsync
import studio.minekarta.titlemanagerreborn.internal.services.task.TaskService
import studio.minekarta.titlemanagerreborn.internal.services.task.TaskServiceSpigot
import studio.minekarta.titlemanagerreborn.internal.services.update.UpdateService
import studio.minekarta.titlemanagerreborn.internal.services.update.UpdateServiceSpigot
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import javax.inject.Singleton
import javax.script.ScriptEngineManager

@Module
object TitleManagerModule {
    @JvmStatic
    @Provides
    @Singleton
    fun providePlugin(): TitleManagerReborn = Bukkit.getPluginManager().getPlugin("TitleManager") as TitleManagerReborn

    @JvmStatic
    @Provides
    @Singleton
    fun provideConfig(plugin: TitleManagerReborn): TMConfigMain = plugin.tmConfig

    @JvmStatic
    @Provides
    @Singleton
    fun provideUpdateService(plugin: TitleManagerReborn): UpdateService = UpdateServiceSpigot(plugin)

    @JvmStatic
    @Provides
    @Singleton
    fun providePlayerInfoService(plugin: TitleManagerReborn): PlayerInfoService = PlayerInfoServiceSqlite(plugin)

    @JvmStatic
    @Provides
    @Singleton
    fun provideTitleManagerService(config: TMConfigMain, listenerService: ListenerService, taskService: TaskService, schedulerService: SchedulerService, animationsService: AnimationsService, scriptService: ScriptService, placeholderService: PlaceholderService, playerListService: PlayerListService, scoreboardService: ScoreboardService, bungeeCordService: BungeeCordService, announcerService: AnnouncerService, metrics: Metrics): TitleManagerService = TitleManagerServiceSpigot(config, listenerService, taskService, schedulerService, animationsService, scriptService, placeholderService, playerListService, scoreboardService, bungeeCordService, announcerService, metrics)

    @JvmStatic
    @Provides
    @Singleton
    fun provideListenerService(config: TMConfigMain, plugin: TitleManagerReborn, taskService: TaskService, updateService: UpdateService, playerInfoService: PlayerInfoService, titleService: TitleService, actionbarService: ActionbarService, playerListService: PlayerListService, scoreboardService: ScoreboardService): ListenerService = ListenerServiceSpigot(config, plugin, taskService, updateService, playerInfoService, titleService, actionbarService, playerListService, scoreboardService)

    @JvmStatic
    @Provides
    @Singleton
    fun provideTaskService(plugin: TitleManagerReborn, config: TMConfigMain, updateService: UpdateService): TaskService = TaskServiceSpigot(plugin, config, updateService)

    @JvmStatic
    @Provides
    @Singleton
    fun provideSchedulerService(): SchedulerService = SchedulerServiceAsync()

    @JvmStatic
    @Provides
    @Singleton
    fun provideAnimationService(plugin: TitleManagerReborn, scriptService: ScriptService, placeholderService: PlaceholderService): AnimationsService = AnimationsServiceFile(plugin, scriptService, placeholderService)

    @JvmStatic
    @Provides
    @Singleton
    fun provideScriptService(plugin: TitleManagerReborn, placeholderService: PlaceholderService, builtinScripts: BuiltinScripts): ScriptService {
        if (ScriptEngineManager().getEngineByName("nashorn") != null) {
            return ScriptServiceNashorn(plugin, placeholderService, builtinScripts)
        }

        return ScriptServiceNotFound(plugin, placeholderService, builtinScripts)
    }

    @JvmStatic
    @Provides
    @Singleton
    fun providePlaceholderService(plugin: TitleManagerReborn, config: TMConfigMain, bungeeCordService: BungeeCordService): PlaceholderService = PlaceholderServiceText(plugin, config, bungeeCordService)

    @JvmStatic
    @Provides
    @Singleton
    fun provideTitleService(plugin: TitleManagerReborn, animationsService: AnimationsService, placeholderService: PlaceholderService, schedulerService: SchedulerService): TitleService = TitleServiceSpigot(plugin, animationsService, placeholderService, schedulerService)

    @JvmStatic
    @Provides
    @Singleton
    fun provideActionbarService(plugin: TitleManagerReborn, placeholderService: PlaceholderService, animationsService: AnimationsService, schedulerService: SchedulerService): ActionbarService = ActionbarServiceSpigot(plugin, placeholderService, animationsService, schedulerService)

    @JvmStatic
    @Provides
    @Singleton
    fun providePlayerListService(plugin: TitleManagerReborn, config: TMConfigMain, placeholderService: PlaceholderService, animationsService: AnimationsService, schedulerService: SchedulerService): PlayerListService = PlayerListServiceSpigot(plugin, config, placeholderService, animationsService, schedulerService)

    @JvmStatic
    @Provides
    @Singleton
    fun provideScoreboardService(plugin: TitleManagerReborn, config: TMConfigMain, placeholderService: PlaceholderService, schedulerService: SchedulerService, animationsService: AnimationsService, playerInfoService: PlayerInfoService): ScoreboardService = if (NMSManager.versionIndex >= 11) {
        ScoreboardServiceSpigot(plugin, config, placeholderService, schedulerService, animationsService, playerInfoService)
    } else {
        ScoreboardServiceSpigotNms(plugin, config, placeholderService, schedulerService, animationsService, playerInfoService)
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideBungeeCordService(plugin: TitleManagerReborn, taskService: TaskService): BungeeCordService = BungeeCordServiceSpigot(plugin, taskService)

    @JvmStatic
    @Provides
    @Singleton
    fun provideAnnouncerService(plugin: TitleManagerReborn, config: TMConfigMain, schedulerService: SchedulerService, titleService: TitleService, actionbarService: ActionbarService): AnnouncerService = AnnouncerServiceSpigot(plugin, config, schedulerService, titleService, actionbarService)

    @JvmStatic
    @Provides
    @Singleton
    fun provideMetrics(plugin: TitleManagerReborn) = Metrics(plugin, 7318)

    @JvmStatic
    @Provides
    @Singleton
    fun builtinScripts() = BuiltinScripts()
}
