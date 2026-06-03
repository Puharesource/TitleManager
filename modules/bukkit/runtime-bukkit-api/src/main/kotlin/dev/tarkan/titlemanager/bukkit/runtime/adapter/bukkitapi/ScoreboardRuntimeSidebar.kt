package dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi

import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeSidebar
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard

internal interface ScoreboardRuntimeSidebarHandler {
    val scoreboard: Scoreboard
    var title: String

    fun get(index: Int): String?

    fun set(index: Int, value: String)

    fun remove(index: Int)

    fun close() {
        scoreboard.objectives.forEach(Objective::unregister)
    }
}

internal class ScoreboardRuntimeSidebar(
    private val server: Server,
    private val owner: Player,
    private val handler: ScoreboardRuntimeSidebarHandler,
    private val requireSamePlayer: Boolean = false
) : RuntimeSidebar {
    override var title: String
        get() = handler.title
        set(value) {
            handler.title = value
        }

    override fun isAppliedTo(player: Player): Boolean {
        return player.scoreboard == handler.scoreboard && (!requireSamePlayer || player.uniqueId == owner.uniqueId)
    }

    override fun get(index: Int): String? = handler.get(index)

    override fun set(index: Int, value: String) {
        handler.set(index, value)
    }

    override fun remove(index: Int) {
        handler.remove(index)
    }

    override fun close() {
        if (isAppliedTo(owner)) {
            owner.scoreboard = server.scoreboardManager.mainScoreboard
        }

        handler.close()
    }
}
