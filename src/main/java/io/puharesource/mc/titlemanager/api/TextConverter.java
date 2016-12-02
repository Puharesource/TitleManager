package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.InternalsKt;
import io.puharesource.mc.titlemanager.TitleManagerPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * @deprecated In favor of the methods seen under the "see also" section.
 *
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#replaceText(Player, String)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#containsPlaceholder(String, String)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#containsPlaceholders(String)
 *
 * @since 1.0.1
 */
@Deprecated
public final class TextConverter {
    /**
     * @deprecated Because it is the inferior method and replaces fewer variables.
     *
     * @param player This is the player that the information will be taken from.
     * @param text This is the text that will be converted.
     *
     * @return The converted text.
     */
    @Deprecated
    public static String setPlayerName(Player player, String text) {
        text = replaceVariable(text, "PLAYER", player.getName());
        text = replaceVariable(text, "DISPLAYNAME", player.getDisplayName());
        text = replaceVariable(text, "STRIPPEDDISPLAYNAME", ChatColor.stripColor(player.getDisplayName()));
        return text;
    }

    @Deprecated
    public static String setVariables(final Player player, final String text) {
        return InternalsKt.getPluginInstance().replaceText(player, text);
    }

    @Deprecated
    public static boolean containsVariable(final String str, final String... strings) {
        final TitleManagerPlugin plugin = InternalsKt.getPluginInstance();

        if (plugin.containsPlaceholders(str)) {
            return true;
        }

        for (final String str0 : strings) {
            if (plugin.containsPlaceholders(str0)) {
                return true;
            }
        }

        return false;
    }

    @Deprecated
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