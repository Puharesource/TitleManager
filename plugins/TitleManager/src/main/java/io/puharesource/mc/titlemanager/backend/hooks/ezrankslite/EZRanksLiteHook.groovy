package io.puharesource.mc.titlemanager.backend.hooks.ezrankslite

import io.puharesource.mc.titlemanager.backend.hooks.PluginHook
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils
import me.clip.ezrankslite.EZAPI
import me.clip.ezrankslite.EZRanksLite
import org.bukkit.Bukkit
import org.bukkit.entity.Player

final class EZRanksLiteHook extends PluginHook {

    EZRanksLiteHook() {
        super("EZRanksLite")
    }

    static EZAPI getEZAPI() { ((EZRanksLite) Bukkit.getPluginManager().getPlugin("EZRanksLite")).getAPI() }

    static String getRankPrefix(Player player) { getEZAPI().getRankData(player).getPrefix() }
    static String getRankupPrefix(Player player) { getEZAPI().getRankupPrefix(player) }
    static String getCurrentRank(Player player) { getEZAPI().getRankData(player).getRank() }
    static String getRankup(Player player) { getEZAPI().getRankupName(player) }
    static String getRankupCost(Player player) { MiscellaneousUtils.formatNumber(getEZAPI().getRankupCost(player)) }
    static String getProgress(Player player) { getEZAPI().getRankupProgress(player) }
    static String getProgressBar(Player player) { getEZAPI().getRankupProgressBar(player) }
    static String getNeeded(Player player) { getEZAPI().getRankData(player).getResetCost() }
}
