package studio.minekarta.titlemanagerreborn.internal.services.announcer

import studio.minekarta.titlemanagerreborn.TitleManagerReborn
import studio.minekarta.titlemanagerreborn.internal.config.TMConfigMain
import studio.minekarta.titlemanagerreborn.internal.extensions.color
import studio.minekarta.titlemanagerreborn.internal.model.announcement.Announcement
import studio.minekarta.titlemanagerreborn.internal.services.features.ActionbarService
import studio.minekarta.titlemanagerreborn.internal.services.features.TitleService
import studio.minekarta.titlemanagerreborn.internal.services.task.SchedulerService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class AnnouncerServiceSpigot(
    private val plugin: TitleManagerReborn,
    private val config: TMConfigMain,
    private val schedulerService: SchedulerService,
    private val titleService: TitleService,
    private val actionbarService: ActionbarService
) : AnnouncerService {
    private val tasks: MutableSet<Int> = mutableSetOf()
    private var isRunning: Boolean = false

    override fun start() {
        require(!isRunning) { "Announcer is already running" }

        isRunning = true

        if (!config.usingConfig) return
        if (!config.announcer.enabled) return

        config.announcer.announcements
            .map { Announcement.fromConfig(it) }
            .filter { !it.isEmpty }
            .forEach { scheduleAnnouncement(it) }
    }

    override fun stop() {
        tasks.forEach { schedulerService.cancel(it) }
        isRunning = false
    }

    private fun scheduleAnnouncement(announcement: Announcement) {
        val index = AtomicInteger(0)

        schedulerService.scheduleRaw(
            {
                val i = index.andIncrement % announcement.size

                plugin.server.onlinePlayers.forEach { player ->
                    if (i < announcement.titles.size) {
                        val titlePair = announcement.titles[i].color().split("\\n", limit = 2)

                        if (titlePair.first().isNotEmpty()) {
                            titleService.sendProcessedTitle(player, titlePair.first(), announcement.fadeIn, announcement.stay, announcement.fadeOut)
                        }

                        if (titlePair.size > 1 && titlePair[1].isNotEmpty()) {
                            titleService.sendProcessedSubtitle(player, titlePair[1], announcement.fadeIn, announcement.stay, announcement.fadeOut)
                        }
                    }

                    if (i < announcement.actionbarTitles.size) {
                        actionbarService.sendProcessedActionbar(player, announcement.actionbarTitles[i].color())
                    }
                }
            },
            announcement.interval,
            announcement.interval,
            TimeUnit.SECONDS
        )
    }
}
