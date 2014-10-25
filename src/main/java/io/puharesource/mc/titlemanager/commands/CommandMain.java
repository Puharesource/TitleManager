package io.puharesource.mc.titlemanager.commands;

import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import io.puharesource.mc.titlemanager.api.TitleObject;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandMain implements CommandExecutor {

    final String PERMISSION_RELOAD = "titlemanager.command.reload";
    final String PERMISSION_BROADCAST = "titlemanager.command.broadcast";
    final String PERMISSION_MESSAGE = "titlemanager.command.message";
    final String PERMISSION_ABROADCAST = "titlemanager.command.abroadcast";
    final String PERMISSION_AMESSAGE = "titlemanager.command.amessage";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("tm")) {
            if (args.length < 1) {
                syntaxError(sender);
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                if (!hasPermission(sender, PERMISSION_RELOAD)) return true;
                Config.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "You've reloaded the config!");
                if (sender instanceof Player)
                    new TitleObject(ChatColor.GREEN + "Config Reloaded!", TitleObject.TitleType.TITLE).setFadeIn(10).setStay(15).setFadeOut(10).send((Player) sender);
            } else if (args[0].equalsIgnoreCase("broadcast") || args[0].equalsIgnoreCase("bc")) {
                if (!hasPermission(sender, PERMISSION_BROADCAST)) return true;
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Wrong usage! Correct usage:");
                    sender.sendMessage(ChatColor.RED + "    /tm bc <message>" + ChatColor.GRAY + " - use <nl> for another line.");
                    return true;
                }
                TitleObject object = getObject(combineArray(1, args));
                for (Player player : Bukkit.getOnlinePlayers())
                    object.send(player);
                if (object.getSubtitle() != null)
                    sender.sendMessage(ChatColor.GREEN + "You have sent a broadcast with the message \"" + object.getTitle() + ChatColor.GREEN + "\" \"" + object.getSubtitle() + "\"");
                else
                    sender.sendMessage(ChatColor.GREEN + "You have sent a broadcast with the message \"" + object.getTitle() + ChatColor.GREEN + "\"");
            } else if (args[0].equalsIgnoreCase("msg")) {
                if (!hasPermission(sender, PERMISSION_MESSAGE)) return true;
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Wrong usage! Correct usage:");
                    sender.sendMessage(ChatColor.RED + "    /tm msg <player> <message>" + ChatColor.GRAY + " - use <nl> for another line.");
                    return true;
                }

                Player player = getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + args[1] + " is not a player!");
                    return true;
                }
                TitleObject object = getObject(combineArray(2, args));
                object.send(player);
                if (object.getSubtitle() != null)
                    sender.sendMessage(ChatColor.GREEN + "You have sent " + ChatColor.stripColor(player.getDisplayName()) + " \"" + object.getTitle() + ChatColor.GREEN + "\" \"" + object.getSubtitle() + ChatColor.GREEN + "\"");
                else
                    sender.sendMessage(ChatColor.GREEN + "You have sent " + ChatColor.stripColor(player.getDisplayName()) + " \"" + object.getTitle() + "\"");
            } else if (args[0].equalsIgnoreCase("abroadcast") || args[0].equalsIgnoreCase("abc")) {
                if (!hasPermission(sender, PERMISSION_ABROADCAST)) return true;
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Wrong usage! Correct usage:");
                    sender.sendMessage(ChatColor.RED + "    /tm abc <message>");
                    return true;
                }
                ActionbarTitleObject object = new ActionbarTitleObject(ChatColor.translateAlternateColorCodes('&', args[1]));
                for (Player player : Bukkit.getOnlinePlayers())
                    object.send(player);
                sender.sendMessage(ChatColor.GREEN + "You have sent an actionbar broadcast with the message \"" + object.getTitle() + ChatColor.GREEN + "\"");
            } else if (args[0].equalsIgnoreCase("amsg")) {
                if (!hasPermission(sender, PERMISSION_AMESSAGE)) return true;
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Wrong usage! Correct usage:");
                    sender.sendMessage(ChatColor.RED + "    /tm amsg <player> <message>");
                    return true;
                }

                Player player = getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + args[1] + " is not a player!");
                    return true;
                }
                ActionbarTitleObject object = new ActionbarTitleObject(combineArray(2, args));
                object.send(player);
                sender.sendMessage(ChatColor.GREEN + "You have sent " + ChatColor.stripColor(player.getDisplayName()) + " \"" + object.getTitle() + "\"");
            } else {
                syntaxError(sender);
            }

            return true;
        }
        return false;
    }

    void syntaxError(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Wrong usage! Correct usages:");
        sender.sendMessage(ChatColor.RED + "    /tm reload" + ChatColor.GRAY + " - Reloads the config.");
        sender.sendMessage(ChatColor.RED + "    /tm bc" + ChatColor.GRAY + " - Broadcasts a title message. (use <nl> for another line)");
        sender.sendMessage(ChatColor.RED + "    /tm msg <player>" + ChatColor.GRAY + " - Sends a title message to the given player. (use <nl> for another line)");
        sender.sendMessage(ChatColor.RED + "    /tm abc" + ChatColor.GRAY + " - Broadcasts an actionbar title message.");
        sender.sendMessage(ChatColor.RED + "    /tm amsg <player>" + ChatColor.GRAY + " - Sends an actionbar title message to the given player.");
    }

    boolean hasPermission(CommandSender sender, String permissionNode) {
        if (!sender.hasPermission(permissionNode)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
            return false;
        }
        return true;
    }

    TitleObject getObject(String title) {
        String subtitle = null;
        if (title.contains("<nl>")) {
            String[] titles = title.split("<nl>", 2);
            title = titles[0];
            subtitle = titles[1];
        }
        if (subtitle == null)
            return new TitleObject(title, TitleObject.TitleType.TITLE);
        else return new TitleObject(title, subtitle);
    }

    String combineArray(int offset, String[] array) {
        StringBuilder sb = new StringBuilder(array[offset]);
        for (int i = offset + 1; array.length > i; i++)
            sb.append(" " + array[i]);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    Player getPlayer(String name) {
        Player correctPlayer = null;
        for (Player player : Bukkit.getOnlinePlayers())
            if (StringUtils.containsIgnoreCase(player.getName(), name)) {
                correctPlayer = player;
                break;
            }
        return correctPlayer;
    }
}
