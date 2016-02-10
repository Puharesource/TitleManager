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

    @SuppressWarnings("deprecation")
    public static EZAPI getEZAPI() {
        return EZRanksLite.getAPI();
    }

    public static String getRankPrefix(final Player player) {
        return getEZAPI().getRankPrefix(player);
    }

    public static String getRankupPrefix(final Player player) {
        return getEZAPI().getRankupPrefix(player);
    }

    public static String getCurrentRank(final Player player) {
        return getEZAPI().getCurrentRank(player);
    }

    public static String getRankup(final Player player) {
        return getEZAPI().getRankupName(player);
    }

    public static String getRankupCost(final Player player) {
        return MiscellaneousUtils.formatNumber(new BigDecimal(getEZAPI().getRankupCost(player)));
    }

    public static String getProgress(final Player player) {
        return String.valueOf(getEZAPI().getRankupProgress(player));
    }

    public static String getProgressBar(final Player player) {
        return getEZAPI().getRankupProgressBar(player);
    }

    public static String getNeeded(final Player player) {
        return getEZAPI().getRankupCostFormatted(player);
    }
}
