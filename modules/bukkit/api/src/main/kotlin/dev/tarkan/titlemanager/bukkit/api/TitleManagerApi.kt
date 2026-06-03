package dev.tarkan.titlemanager.bukkit.api

import dev.tarkan.titlemanager.time.Timing
import org.bukkit.entity.Player

interface TitleManagerApi {
    fun showTitle(player: Player, title: String, subtitle: String, timing: Timing = Timing.default): TitleManagerSession

    fun sendActionbar(player: Player, message: String): TitleManagerSession

    fun setPlayerListHeaderAndFooter(player: Player, header: String, footer: String): TitleManagerSession

    /**
     * Applies a managed sidebar. Bukkit scoreboards support at most 15 visible lines.
     *
     * @throws IllegalArgumentException when more than 15 lines are provided.
     */
    fun setSidebar(player: Player, title: String, lines: List<String>): TitleManagerSession

    fun clearTitle(player: Player)

    fun clearActionbar(player: Player)

    fun clearPlayerListHeaderAndFooter(player: Player)

    fun clearSidebar(player: Player)
}
