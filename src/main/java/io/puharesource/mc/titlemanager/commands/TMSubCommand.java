package io.puharesource.mc.titlemanager.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public abstract class TMSubCommand {

    private String alias;
    private String node;
    private String usage;
    private String description;
    private String[] aliases;

    public TMSubCommand(String alias, String node, String usage, String description, String... aliases) {
        this.alias = alias;
        this.node = node;
        this.usage = usage;
        this.description = description;
        this.aliases = aliases;
    }

    public abstract void onCommand(CommandSender sender, String[] args);

    public void syntaxError(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Wrong usage! Correct usage:");
        sender.sendMessage(ChatColor.RED + "   /" + getAlias() + " " + getUsage());
    }

    public String getAlias() {
        return alias;
    }

    public String getNode() {
        return node;
    }

    public String getUsage() {
        return usage;
    }

    public String getDescription() {
        return description;
    }

    public String[] getAliases() {
        return aliases;
    }
}
