package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.TitleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class TextConverter {
    /**
     * @deprecated Because it is the inferior method and replaces fewer variables.
     */
    @Deprecated
    public static String setPlayerName(Player player, String text) {
        text = replaceVariable(text, "PLAYER", player.getName());
        text = replaceVariable(text, "DISPLAYNAME", player.getDisplayName());
        text = replaceVariable(text, "STRIPPEDDISPLAYNAME", ChatColor.stripColor(player.getDisplayName()));
        return text;
    }

    public static String setVariables(Player player, String text) {
        if (!text.contains("{") && !text.contains("}")) return text;

        if (player != null) {
            text = replaceVariable(text, TitleVariable.PLAYER_NAME, player.getName());
            text = replaceVariable(text, TitleVariable.DISPLAY_NAME, player.getDisplayName());
            text = replaceVariable(text, TitleVariable.STRIPPED_DISPLAY_NAME, ChatColor.stripColor(player.getDisplayName()));
            text = replaceVariable(text, TitleVariable.WORLD, player.getWorld().getName());
            text = replaceVariable(text, TitleVariable.WORLD_TIME, Long.toString(player.getWorld().getTime()));

            if (TitleManager.isVaultEnabled()) {
                if (TitleManager.isEconomySupported()) {
                    text = replaceVariable(text, TitleVariable.BALANCE, Config.isNumberFormatEnabled() ? formatNumber(TitleManager.getEconomy().getBalance(player)) : String.valueOf(TitleManager.getEconomy().getBalance(player)));
                }

                if (TitleManager.isPermissionsSupported() && TitleManager.getPermissions().hasGroupSupport())
                    text = replaceVariable(text, TitleVariable.GROUP_NAME, TitleManager.getPermissions().getPrimaryGroup(player));
            }
        }

        text = replaceVariable(text, TitleVariable.ONLINE_PLAYERS, String.valueOf(Bukkit.getOnlinePlayers().size()));
        text = replaceVariable(text, TitleVariable.MAX_PLAYERS, String.valueOf(Bukkit.getMaxPlayers()));

        return text;
    }

    public static boolean containsVariable(String str, String... strings) {
        if (str != null && (str.contains("{") || str.contains("}"))) return true;

        for (String str0 : strings)
            if (str0.contains("{") || str0.contains("}")) return true;

        return false;
    }

    private static String replaceVariable(String text, String variable, String replacement) {
        try {
            if (text.toLowerCase().contains("{" + variable.toLowerCase() + "}"))
                return text.replaceAll("(?i)\\{" + variable + "\\}", replacement);
            else return text;
        } catch (Exception e) {
            return text;
        }
    }

    private static String replaceVariable(String text, TitleVariable variable, String replacement) {
        return replaceVariable(text, variable.getText(), replacement);
    }
    
    private static String formatNumber(double number) {
        return formatNumber(new BigDecimal(number));
    }
    
    private static String formatNumber(BigDecimal number) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        return new DecimalFormat(Config.getNumberFormat(), symbols).format(number);
    }
}
