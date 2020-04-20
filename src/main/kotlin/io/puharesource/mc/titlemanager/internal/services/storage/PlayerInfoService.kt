package io.puharesource.mc.titlemanager.internal.services.storage

import org.bukkit.entity.Player

interface PlayerInfoService {
    fun isScoreboardEnabled(player: Player): Boolean
    fun showScoreboard(player: Player, show: Boolean)
}
