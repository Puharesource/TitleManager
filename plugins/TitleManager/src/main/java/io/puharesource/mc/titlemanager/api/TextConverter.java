package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.TitleManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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

    public static String setVariables(final Player player, final String text) {
        if (!containsVariable(text)) return text;

        return TitleManager.getInstance().getVariableManager().replaceText(player, text);
    }

    public static boolean containsVariable(String str, String... strings) {
        if (str != null && str.contains("{") && str.contains("}")) return true;

        for (String str0 : strings)
            if (str != null && str0.contains("{") || str0.contains("}")) return true;

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
}
