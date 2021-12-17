package io.puharesource.mc.titlemanager.internal.modules

import dagger.Module
import dagger.Provides
import io.puharesource.mc.titlemanager.TitleManagerPlugin
import io.puharesource.mc.titlemanager.internal.config.TMConfigMain
import io.puharesource.mc.titlemanager.internal.model.script.BuiltinScripts
import io.puharesource.mc.titlemanager.internal.reflections.NMSManager
import io.puharesource.mc.titlemanager.internal.services.TitleManagerService
import io.puharesource.mc.titlemanager.internal.services.TitleManagerServiceSpigot
import io.puharesource.mc.titlemanager.internal.services.animation.AnimationsService
import io.puharesource.mc.titlemanager.internal.services.animation.AnimationsServiceFile
import io.puharesource.mc.titlemanager.internal.services.animation.ScriptService
import io.puharesource.mc.titlemanager.internal.services.animation.ScriptServiceNashorn
import io.puharesource.mc.titlemanager.internal.services.animation.ScriptServiceNotFound
import io.puharesource.mc.titlemanager.internal.services.announcer.AnnouncerService
import io.puharesource.mc.titlemanager.internal.services.announcer.AnnouncerServiceSpigot
import io.puharesource.mc.titlemanager.internal.services.bungeecord.BungeeCordService
import io.puharesource.mc.titlemanager.internal.services.bungeecord.BungeeCordServiceSpigot
import io.puharesource.mc.titlemanager.internal.services.event.ListenerService
import io.puharesource.mc.titlemanager.internal.services.event.ListenerServiceSpigot
import io.puharesource.mc.titlemanager.internal.services.features.ActionbarService
import io.puharesource.mc.titlemanager.internal.services.features.ActionbarServiceSpigot
import io.puharesource.mc.titlemanager.internal.services.features.PlayerListService
import io.puharesource.mc.titlemanager.internal.services.features.PlayerListServiceSpigot
import io.puharesource.mc.titlemanager.internal.services.features.ScoreboardService
import io.puharesource.mc.titlemanager.internal.services.features.ScoreboardServiceSpigotNms
import io.puharesource.mc.titlemanager.internal.services.features.ScoreboardServiceSpigot
import io.puharesource.mc.titlemanager.internal.services.features.TitleService
import io.puharesource.mc.titlemanager.internal.services.features.TitleServiceSpigot
import io.puharesource.mc.titlemanager.internal.services.placeholder.PlaceholderService
import io.puharesource.mc.titlemanager.internal.services.placeholder.PlaceholderServiceText
import io.puharesource.mc.titlemanager.internal.services.storage.PlayerInfoService
import io.puharesource.mc.titlemanager.internal.services.storage.PlayerInfoServiceSqlite
import io.puharesource.mc.titlemanager.internal.services.task.SchedulerService
import io.puharesource.mc.titlemanager.internal.services.task.SchedulerServiceAsync
import io.puharesource.mc.titlemanager.internal.services.task.TaskService
import io.puharesource.mc.titlemanager.internal.services.task.TaskServiceSpigot
import io.puharesource.mc.titlemanager.internal.services.update.UpdateService
import io.puharesource.mc.titlemanager.internal.services.update.UpdateServiceSpigot
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import javax.inject.Singleton
import javax.script.ScriptEngineManager

@Module
object TitleManagerModule {
    @JvmStatic
    @Provides
    @Singleton
    fun providePlugin(): io.puharesource.mc.titlemanager.TitleManagerPlugin = Bukkit.getPluginManager().getPlugin("TitleManager") as io.puharesource.mc.titlemanager.TitleManagerPlugin

    @JvmStatic
    @Provides
    @Singleton
    fun provideConfig(plugin: io.puharesource.mc.titlemanager.TitleManagerPlugin): TMConfigMain = plugin.tmConfig

    @JvmStatic
    @Provides
    @Singleton
    fun provideUpdateService(plugin: io.puharesource.mc.titlemanager.TitleManagerPlugin): UpdateService = UpdateServiceSpigot(plugin)

    @JvmStatic
    @Provides
    @Singleton
    fun providePlayerInfoService(plugin: io.puharesource.mc.titlemanager.TitleManagerPlugin): PlayerInfoService = PlayerInfoServiceSqlite(plugin)

    @JvmStatic
    @Provides
    @Singleton
    fun provideTitleManagerService(config: TMConfigMain, listenerService: ListenerService, taskService: TaskService, schedulerService: SchedulerService, animationsService: AnimationsService, scriptService: ScriptService, placeholderService: PlaceholderService, playerListService: PlayerListService, scoreboardService: ScoreboardService, bungeeCordService: BungeeCordService, announcerService: AnnouncerService, metrics: Metrics): TitleManagerService = TitleManagerServiceSpigot(config, listenerService, taskService, schedulerService, animationsService, scriptService, placeholderService, playerListService, scoreboardService, bungeeCordService, announcerService, metrics)

    @JvmStatic
    @Provides
    @Singleton
    fun provideListenerService(config: TMConfigMain, plugin: io.puharesource.mc.titlemanager.TitleManagerPlugin, taskService: TaskService, updateService: UpdateService, playerInfoService: PlayerInfoService, titleService: TitleService, actionbarService: ActionbarService, playerListService: PlayerListService, scoreboardService: ScoreboardService): ListenerService = ListenerServiceSpigot(config, plugin, taskService, updateService, playerInfoService, titleService, actionbarService, playerListService, scoreboardService)

    @JvmStatic
    @Provides
    @Singleton
    fun provideTaskService(plugin: io.puharesource.mc.titlemanager.TitleManagerPlugin, config: TMConfigMain, updateService: UpdateService): TaskService = TaskServiceSpigot(plugin, config, updateService)

    @JvmStatic
    @Provides
    @Singleton
    fun provideSchedulerService(): SchedulerService = SchedulerServiceAsync()

    @JvmStatic
    @Provides
    @Singleton
    fun provideAnimationService(plugin: io.puharesource.mc.titlemanager.TitleManagerPlugin, scriptService: ScriptService, placeholderService: PlaceholderService): AnimationsService = AnimationsServiceFile(plugin, scriptService, placeholderService)

    @JvmStatic
    @Provides
    @Singleton
    fun provideScriptService(plugin: io.puharesource.mc.titlemanager.TitleManagerPlugin, placeholderService: PlaceholderService, builtinScripts: BuiltinScripts): ScriptService {
        if (ScriptEngineManager().getEngineByName("nashorn") != null) {
            return ScriptServiceNashorn(plugin, placeholderService, builtinScripts)
        }

        return ScriptServiceNotFound(plugin, placeholderService, builtinScripts)
    }

    @JvmStatic
    @Provides
    @Singleton
    fun providePlaceholderService(plugin: io.puharesource.mc.titlemanager.TitleManagerPlugin, config: TMConfigMain, bungeeCordService: BungeeCordService): PlaceholderService = PlaceholderServiceText(plugin, config, bungeeCordService)

    @JvmStatic
    @Provides
    @Singleton
    fun provideTitleService(plugin: io.puharesource.mc.titlemanager.TitleManagerPlugin, animationsService: AnimationsService, placeholderService: PlaceholderService, schedulerService: SchedulerService): TitleService = TitleServiceSpigot(plugin, animationsService, placeholderService, schedulerService)

    @JvmStatic
    @Provides
    @Singleton
    fun provideActionbarService(plugin: io.puharesource.mc.titlemanager.TitleManagerPlugin, placeholderService: PlaceholderService, animationsService: AnimationsService, schedulerService: SchedulerService): ActionbarService = ActionbarServiceSpigot(plugin, placeholderService, animationsService, schedulerService)

    @JvmStatic
    @Provides
    @Singleton
    fun providePlayerListService(plugin: io.puharesource.mc.titlemanager.TitleManagerPlugin, config: TMConfigMain, placeholderService: PlaceholderService, animationsService: AnimationsService, schedulerService: SchedulerService): PlayerListService = PlayerListServiceSpigot(plugin, config, placeholderService, animationsService, schedulerService)

    @JvmStatic
    @Provides
    @Singleton
    fun provideScoreboardService(plugin: io.puharesource.mc.titlemanager.TitleManagerPlugin, config: TMConfigMain, placeholderService: PlaceholderService, schedulerService: SchedulerService, animationsService: AnimationsService, playerInfoService: PlayerInfoService): ScoreboardService = if (NMSManager.versionIndex >= 11) {
        ScoreboardServiceSpigot(plugin, config, placeholderService, schedulerService, animationsService, playerInfoService)
    } else {
        ScoreboardServiceSpigotNms(plugin, config, placeholderService, schedulerService, animationsService, playerInfoService)
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideBungeeCordService(plugin: io.puharesource.mc.titlemanager.TitleManagerPlugin, taskService: TaskService): BungeeCordService = BungeeCordServiceSpigot(plugin, taskService)

    @JvmStatic
    @Provides
    @Singleton
    fun provideAnnouncerService(plugin: io.puharesource.mc.titlemanager.TitleManagerPlugin, config: TMConfigMain, schedulerService: SchedulerService, titleService: TitleService, actionbarService: ActionbarService): AnnouncerService = AnnouncerServiceSpigot(plugin, config, schedulerService, titleService, actionbarService)

    @JvmStatic
    @Provides
    @Singleton
    fun provideMetrics(plugin: io.puharesource.mc.titlemanager.TitleManagerPlugin) = Metrics(plugin, 7318)

    @JvmStatic
    @Provides
    @Singleton
    fun builtinScripts() = BuiltinScripts()
}
