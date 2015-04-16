package io.puharesource.mc.titlemanager.backend.hooks.ezrankslite;

import io.puharesource.mc.titlemanager.backend.hooks.PluginHook;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import me.clip.ezrankslite.EZAPI;
import me.clip.ezrankslite.EZRanksLite;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public final class EZRanksLiteHook extends PluginHook {
    public EZRanksLiteHook() {
        super("EZRanksLite");
    }

    public static EZAPI getEZAPI() {
        return EZRanksLite.getAPI();
    }

    public static String getRankPrefix(Player player) {
        return getEZAPI().getRankData(player).getPrefix();
    }

    public static String getRankupPrefix(Player player) {
        return getEZAPI().getRankupPrefix(player);
    }

    public static String getCurrentRank(Player player) {
        return getEZAPI().getRankData(player).getRank();
    }

    public static String getRankup(Player player) {
        return getEZAPI().getRankupName(player);
    }

    public static String getRankupCost(Player player) {
        return MiscellaneousUtils.formatNumber(new BigDecimal(getEZAPI().getRankupCost(player)));
    }

    public static String getProgress(Player player) {
        return String.valueOf(getEZAPI().getRankupProgress(player));
    }

    public static String getProgressBar(Player player) {
        return getEZAPI().getRankupProgressBar(player);
    }

    public static String getNeeded(Player player) {
        return getEZAPI().getRankData(player).getResetCost();
    }

}
