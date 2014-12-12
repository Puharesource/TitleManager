package io.puharesource.mc.titlemanager.api;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TextConverter {
    public static String convert(String text) {
        if (text == null || text.length() == 0)
            return "\"\"";

        char c;
        int i;
        int len = text.length();
        StringBuilder sb = new StringBuilder(len + 4);
        String t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            c = text.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

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
        text = replaceVariable(text, TitleVariable.getFromString("PLAYER"), player.getName());
        text = replaceVariable(text, TitleVariable.getFromString("DISPLAYNAME"), player.getDisplayName());
        text = replaceVariable(text, TitleVariable.getFromString("STRIPPEDDISPLAYNAME"), ChatColor.stripColor(player.getDisplayName()));
        text = replaceVariable(text, TitleVariable.getFromString("WORLD"), player.getWorld().getName());
        text = replaceVariable(text, TitleVariable.getFromString("WORLD TIME"), Long.toString(player.getWorld().getTime()));
        text = replaceVariable(text, TitleVariable.getFromString("GROUP_NAME"), /*TODO vault integration OR custom method (Vault preferred)*/"");
        text = replaceVariable(text, TitleVariable.getFromString("ONLINE"), Integer.toString(Bukkit.getOnlinePlayers().size()));
        text = replaceVariable(text, TitleVariable.getFromString("MAX PLAYERS"), Integer.toString(Bukkit.getMaxPlayers()));
        text = replaceVariable(text, TitleVariable.getFromString("BALANCE"),/*TODO vault integration*/"");
        text = replaceVariable(text, TitleVariable.getFromString("RAINBOW"),/*This requires animation which is not quite done yet.*/"");
        text = replaceVariable(text, TitleVariable.getFromString("ONLINE: servername|ALL"),/*Bungee needed for this. TODO add check for bungee and add string accordingly.*/"");
        text = replaceVariable(text, TitleVariable.getFromString("MAX: servername|ALL"),/*Bungee needed for this.*/"");
        return text;
    }

    /**
     * @deprecated because it is replaced by the better method using enumerators.
     */
    @Deprecated
    static String replaceVariable(String str0, String variable, String str1) {
        try {
            if (str0.toLowerCase().contains("{" + variable.toLowerCase() + "}"))
                return str0.replaceAll("(?i)\\{" + variable + "\\}", str1);
            else return str0;
        } catch (Exception e) {
            return str0;
        }
    }

    private static String replaceVariable(String main, TitleVariable variable, String replacer) {
        if (variable == null || main == null || replacer == null) return "";
        if (main.equals("") || replacer.equals("")) return "";
        try {
            if (main.toLowerCase().contains("{" + variable.getText().toLowerCase() + "}"))
                return main.replaceAll("(?i)\\{" + variable.getText() + "\\}", replacer);
            else return main;
        } catch (Exception e) {
            return main;
        }
    }
}
