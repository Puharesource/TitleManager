package io.puharesource.mc.titlemanager.commands;

import io.puharesource.mc.titlemanager.Config;
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

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("tm")) {
            if(args.length < 1) {
                syntaxError(sender);
                return true;
            }

            if(args[0].equalsIgnoreCase("reload")) {
                if(!hasPermission(sender, PERMISSION_RELOAD)) return true;
                Config.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "You've reloaded the config!");
                if(sender instanceof Player)
                    new TitleObject(ChatColor.GREEN + "Config Reloaded!", TitleObject.TitleType.TITLE).setFadeIn(10).setStay(15).setFadeOut(10).send((Player) sender);
            } else if(args[0].equalsIgnoreCase("broadcast")) {
                if(!hasPermission(sender, PERMISSION_BROADCAST)) return true;
                if(args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Wrong usage! Correct usage:");
                    sender.sendMessage(ChatColor.RED + "    /tm broadcast <message>" + ChatColor.GRAY + " - use \\n for another line.");
                    return true;
                }
                TitleObject object = getObject(args, 1);
                for(Player player : Bukkit.getOnlinePlayers())
                    object.send(player);
                if(object.getSubtitle() != null)
                    sender.sendMessage(ChatColor.GREEN + "You have sent a broadcast with the message \"" + object.getTitle() + ChatColor.GREEN + "\" \"" + object.getSubtitle() + "\"");
                else sender.sendMessage(ChatColor.GREEN + "You have sent a broadcast with the message \"" + object.getTitle() + ChatColor.GREEN + "\"");
            } else if(args[0].equalsIgnoreCase("msg")) {
                if(!hasPermission(sender, PERMISSION_MESSAGE)) return true;
                if(args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Wrong usage! Correct usage:");
                    sender.sendMessage(ChatColor.RED + "    /tm msg <player> <message>" + ChatColor.GRAY + " - use \\n for another line.");
                    return true;
                }

                Player player = getPlayer(args[1]);
                if(player == null) {
                    sender.sendMessage(ChatColor.RED + args[1] + " is not a player!");
                    return true;
                }
                TitleObject object = getObject(args, 2);
                object.send(player);
                if(object.getSubtitle() != null)
                    sender.sendMessage(ChatColor.GREEN + "You have sent " + ChatColor.stripColor(player.getDisplayName()) + " \"" + object.getTitle() + ChatColor.GREEN + "\" \"" + object.getSubtitle() + ChatColor.GREEN + "\"");
                else sender.sendMessage(ChatColor.GREEN + "You have sent " + ChatColor.stripColor(player.getDisplayName()) + " \"" + object.getTitle() + "\"");
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
        sender.sendMessage(ChatColor.RED + "    /tm broadcast" + ChatColor.GRAY + " - Broadcasts a title message. (use <nl> for another line)");
        sender.sendMessage(ChatColor.RED + "    /tm msg <player>" + ChatColor.GRAY + " - Sends a title message to the given player. (use <nl> for another line)");
    }

    boolean hasPermission(CommandSender sender, String permissionNode) {
        if(!sender.hasPermission(permissionNode)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
            return false;
        }
        return true;
    }

    TitleObject getObject(String[] args, int start) {
        StringBuilder sb = new StringBuilder(args[start]);
        for(int i = start + 1; args.length > i; i++)
            sb.append(" " + args[i]);

        String title = ChatColor.translateAlternateColorCodes('&', sb.toString());
        String subtitle = null;
        if(title.contains("<nl>")) {
            String[] titles = title.split("<nl>", 2);
            title = titles[0];
            subtitle = titles[1];
        }
        if(subtitle == null)
            return new TitleObject(title, TitleObject.TitleType.TITLE);
        else return new TitleObject(title, subtitle);
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
