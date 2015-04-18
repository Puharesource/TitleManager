package io.puharesource.mc.titlemanager.backend.variables.replacers;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.backend.player.TMPlayer;
import io.puharesource.mc.titlemanager.api.variables.Variable;
import io.puharesource.mc.titlemanager.api.variables.VariableReplacer;
import io.puharesource.mc.titlemanager.backend.hooks.specialrules.VanishRule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class VariablesDefault implements VariableReplacer {
    @Variable(vars = {"PLAYER", "USERNAME", "NAME"})
    public String nameVar(Player player) { return player.getName(); }

    @Variable(vars = {"DISPLAYNAME", "DISPLAY-NAME", "NICKNAME", "NICK"})
    public String displayNameVar(Player player) { return player.getDisplayName(); }

    @Variable(vars = {"STRIPPEDDISPLAYNAME", "STRIPPED-DISPLAYNAME", "STRIPPED-NICKNAME", "STRIPPED-NICK"})
    public String strippedDisplayNameVar(Player player) { return ChatColor.stripColor(player.getDisplayName()); }

    @Variable(vars = {"WORLD", "WORLD-NAME"})
    public String worldNameVar(Player player) { return player.getWorld().getName(); }

    @Variable(vars = {"WORLD-TIME"})
    public String worldTimeVar(Player player) { return String.valueOf(player.getWorld().getTime()); }

    @Variable(vars = {"ONLINE", "ONLINE-PLAYERS"})
    public String playerCountVar(Player player) { return String.valueOf(Bukkit.getOnlinePlayers().size()); }

    @Variable(vars = {"MAX-PLAYERS"})
    public String maxOnlineVar(Player player) { return String.valueOf(Bukkit.getMaxPlayers()); }

    @Variable(vars = {"WORLD-PLAYERS", "WORLD-ONLINE"})
    public String worldPlayerCountVar(Player player) { return String.valueOf(player.getWorld().getPlayers().size()); }

    @Variable(vars = {"SERVER-TIME"})
    public String serverTimeVar(Player player) { return new SimpleDateFormat(TitleManager.getInstance().getConfig().getString("date-format.format")).format(new Date(System.currentTimeMillis())); }

    @Variable(rule = "VANISH-RULE", vars = {"SAFE-ONLINE", "SAFE-ONLINE-PLAYERS"})
    public String safeOnlineVar(Player player) { return String.valueOf(VanishRule.getOnlinePlayers()); }

    @Variable(vars = {"PING"})
    public String pingVar(Player player) { return String.valueOf(new TMPlayer(player).getPing()); }
}
