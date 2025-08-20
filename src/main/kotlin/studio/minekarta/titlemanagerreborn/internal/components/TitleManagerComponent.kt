package studio.minekarta.titlemanagerreborn.internal.components

import dagger.Component
import studio.minekarta.titlemanagerreborn.internal.model.script.BuiltinScripts
import studio.minekarta.titlemanagerreborn.internal.modules.TitleManagerModule
import studio.minekarta.titlemanagerreborn.internal.services.TitleManagerService
import studio.minekarta.titlemanagerreborn.internal.services.animation.AnimationsService
import studio.minekarta.titlemanagerreborn.internal.services.animation.ScriptService
import studio.minekarta.titlemanagerreborn.internal.services.announcer.AnnouncerService
import studio.minekarta.titlemanagerreborn.internal.services.bungeecord.BungeeCordService
import studio.minekarta.titlemanagerreborn.internal.services.event.ListenerService
import studio.minekarta.titlemanagerreborn.internal.services.features.ActionbarService
import studio.minekarta.titlemanagerreborn.internal.services.features.PlayerListService
import studio.minekarta.titlemanagerreborn.internal.services.features.ScoreboardService
import studio.minekarta.titlemanagerreborn.internal.services.features.TitleService
import studio.minekarta.titlemanagerreborn.internal.services.placeholder.PlaceholderService
import studio.minekarta.titlemanagerreborn.internal.services.storage.PlayerInfoService
import studio.minekarta.titlemanagerreborn.internal.services.task.SchedulerService
import studio.minekarta.titlemanagerreborn.internal.services.task.TaskService
import studio.minekarta.titlemanagerreborn.internal.services.update.UpdateService
import javax.inject.Singleton

@Singleton
@Component(modules = [TitleManagerModule::class])
interface TitleManagerComponent {
    @Singleton
    fun titleManagerService(): TitleManagerService

    @Singleton
    fun updateService(): UpdateService

    @Singleton
    fun playerInfoService(): PlayerInfoService

    @Singleton
    fun listenerService(): ListenerService

    @Singleton
    fun taskService(): TaskService

    @Singleton
    fun schedulerService(): SchedulerService

    @Singleton
    fun animationsService(): AnimationsService

    @Singleton
    fun scriptService(): ScriptService

    @Singleton
    fun placeholderService(): PlaceholderService

    @Singleton
    fun titleService(): TitleService

    @Singleton
    fun actionbarService(): ActionbarService

    @Singleton
    fun playerListService(): PlayerListService

    @Singleton
    fun scoreboardService(): ScoreboardService

    @Singleton
    fun bungeeCordService(): BungeeCordService

    @Singleton
    fun announcerService(): AnnouncerService

    @Singleton
    fun builtinScripts(): BuiltinScripts
}
