package dev.tarkan.titlemanager.bukkit.announcer

import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.configuration.AnnouncerConfiguration
import dev.tarkan.titlemanager.bukkit.configuration.AnnouncerEntryConfiguration
import dev.tarkan.titlemanager.bukkit.context.PlayerContext
import dev.tarkan.titlemanager.bukkit.context.PlayerContextManager
import dev.tarkan.titlemanager.bukkit.extensions.splitTypedLineBreak
import dev.tarkan.titlemanager.time.Timing
import org.bukkit.scheduler.BukkitTask
import java.io.Closeable

class AnnouncerService(
    private val plugin: TitleManagerPlugin,
    private val configuration: AnnouncerConfiguration,
    private val playerContextManager: PlayerContextManager
) : Closeable {
    private val tasks = mutableListOf<BukkitTask>()
    private val indexesByAnnouncement = mutableMapOf<String, Int>()

    fun start() {
        if (!configuration.enabled) {
            return
        }

        configuration.announcements
            .filterValues { it.titles.isNotEmpty() || it.actionbar.isNotEmpty() }
            .forEach { (name, announcement) ->
                val intervalTicks = announcement.interval * TICKS_PER_SECOND
                val task = plugin.server.scheduler.runTaskTimer(
                    plugin,
                    Runnable { sendAnnouncement(name, announcement) },
                    intervalTicks,
                    intervalTicks
                )

                tasks += task
            }
    }

    private fun sendAnnouncement(name: String, announcement: AnnouncerEntryConfiguration) {
        val size = maxOf(announcement.titles.size, announcement.actionbar.size)
        if (size == 0) {
            return
        }

        val index = indexesByAnnouncement.getOrDefault(name, 0) % size
        indexesByAnnouncement[name] = index + 1

        plugin.server.onlinePlayers
            .filter { it.isOnline }
            .map(playerContextManager::getContext)
            .forEach { context ->
                announcement.titles.getOrNull(index)?.let { title ->
                    context.sendAnnouncementTitle(title, announcement.timing())
                }

                announcement.actionbar.getOrNull(index)?.let { actionbar ->
                    context.sendActionbar(actionbar)
                }
            }
    }

    private fun PlayerContext.sendAnnouncementTitle(title: String, timing: Timing) {
        val parts = title.splitTypedLineBreak(2)
        val titlePart = parts.getOrElse(0) { "" }
        val subtitlePart = parts.getOrElse(1) { "" }

        when {
            titlePart.isNotEmpty() && subtitlePart.isNotEmpty() -> sendTitleAndSubtitle(titlePart, subtitlePart, timing)
            titlePart.isNotEmpty() -> sendTitle(titlePart, timing)
            subtitlePart.isNotEmpty() -> sendSubtitle(subtitlePart, timing)
        }
    }

    private fun AnnouncerEntryConfiguration.timing(): Timing {
        return Timing(
            (timings.fadeIn * 50).toUInt(),
            (timings.stay * 50).toUInt(),
            (timings.fadeOut * 50).toUInt()
        )
    }

    override fun close() {
        tasks.forEach(BukkitTask::cancel)
        tasks.clear()
        indexesByAnnouncement.clear()
    }

    private companion object {
        const val TICKS_PER_SECOND = 20L
    }
}
