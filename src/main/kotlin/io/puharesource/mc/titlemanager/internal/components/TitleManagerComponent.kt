package io.puharesource.mc.titlemanager.internal.components

import dagger.Component
import io.puharesource.mc.titlemanager.internal.modules.TitleManagerModule
import io.puharesource.mc.titlemanager.internal.services.TitleManagerService
import io.puharesource.mc.titlemanager.internal.services.animation.AnimationsService
import io.puharesource.mc.titlemanager.internal.services.animation.ScriptService
import io.puharesource.mc.titlemanager.internal.services.announcer.AnnouncerService
import io.puharesource.mc.titlemanager.internal.services.bungeecord.BungeeCordService
import io.puharesource.mc.titlemanager.internal.services.event.ListenerService
import io.puharesource.mc.titlemanager.internal.services.features.ActionbarService
import io.puharesource.mc.titlemanager.internal.services.features.PlayerListService
import io.puharesource.mc.titlemanager.internal.services.features.ScoreboardService
import io.puharesource.mc.titlemanager.internal.services.features.TitleService
import io.puharesource.mc.titlemanager.internal.services.placeholder.PlaceholderService
import io.puharesource.mc.titlemanager.internal.services.storage.PlayerInfoService
import io.puharesource.mc.titlemanager.internal.services.task.SchedulerService
import io.puharesource.mc.titlemanager.internal.services.task.TaskService
import io.puharesource.mc.titlemanager.internal.services.update.UpdateService
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
}
