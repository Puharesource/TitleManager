package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.backend.bungee.BungeeServerInfo;
import io.puharesource.mc.titlemanager.backend.hooks.PluginHook;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class TextConverter {
    /**
     * @deprecated Because it is the inferior method and replaces fewer variables.
     * @param player This is the player that the information will be taken from.
     * @param text This is the text that will be converted.
     * @return The converted text.
     */
    @Deprecated
    public static String setPlayerName(Player player, String text) {
        text = replaceVariable(text, "PLAYER", player.getName());
        text = replaceVariable(text, "DISPLAYNAME", player.getDisplayName());
        text = replaceVariable(text, "STRIPPEDDISPLAYNAME", ChatColor.stripColor(player.getDisplayName()));
        return text;
    }

    public static String setVariables(final Player player, final String text) {
        if (!containsVariable(text)) return text;

        String replacedText = TitleManager.getInstance().getVariableManager().replaceText(player, text);

        final PluginHook placeholderAPIHook = TitleManager.getInstance().getVariableManager().getHook("PLACEHOLDERAPI");
        if (placeholderAPIHook != null && placeholderAPIHook.isEnabled()) {
            replacedText = PlaceholderAPI.setPlaceholders(player, replacedText);
        }

        if (TitleManager.getInstance().getConfigManager().getConfig().usingBungeecord) {
            for (final BungeeServerInfo server : TitleManager.getInstance().getBungeeManager().getServers().values()) {
                replacedText = replaceVariable(replacedText, "ONLINE:" + server.getName(), String.valueOf(server.getPlayerCount()));
                replacedText = replaceVariable(replacedText, "ONLINE-PLAYERS:" + server.getName(), String.valueOf(server.getPlayerCount()));
            }
        }

        return replacedText;
    }

    public static boolean containsVariable(final String str, final String... strings) {
        if (str != null && ((str.contains("{") && str.contains("}")) || str.contains("%"))) return true;

        for (final String str0 : strings)
            if (str0 != null && ((str0.contains("{") && str0.contains("}")) || str0.contains("%"))) return true;

        return false;
    }

    private static String replaceVariable(final String text, final String variable, final String replacement) {
        try {
            if (text.toLowerCase().contains("{" + variable.toLowerCase() + "}"))
                return text.replaceAll("(?i)\\{" + variable + "\\}", replacement);
            else return text;
        } catch (Exception e) {
            return text;
        }
    }
}
