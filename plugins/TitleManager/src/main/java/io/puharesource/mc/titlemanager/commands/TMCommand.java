package io.puharesource.mc.titlemanager.commands;

import io.puharesource.mc.titlemanager.TitleManager;
import org.bukkit.ChatColor;
import org.bukkit.command.*;

import java.util.*;

public class TMCommand implements CommandExecutor, TabCompleter {

    private Map<String, TMSubCommand> commands = new HashMap<>();

    public TMCommand() {
        PluginCommand cmd = TitleManager.getInstance().getCommand("tm");
        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
    }

    public void addSubCommand(TMSubCommand cmd) {
        commands.put(cmd.getAlias().toUpperCase(), cmd);
        for (String alias : cmd.getAliases())
            commands.put(alias.toUpperCase(), cmd);
    }

    private void syntaxError(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Wrong usage! Correct usages: ");
        List<String> aliases = new ArrayList<>();
        for (TMSubCommand cmd : commands.values()) {
            if (aliases.contains(cmd.getAlias().toUpperCase())) continue;
            sender.sendMessage(ChatColor.RED + "    /tm " + cmd.getAlias() + " " + cmd.getUsage() + ChatColor.GRAY + (cmd.getUsage().isEmpty() ? "- " : " - ") + cmd.getDescription());
            aliases.add(cmd.getAlias().toUpperCase());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("tm")) return false;
        if (args.length >= 1) {
            TMSubCommand subCommand = commands.get(args[0].toUpperCase());
            if (subCommand != null)
                if (sender.hasPermission(subCommand.getNode()))
                    subCommand.onCommand(sender, Arrays.copyOfRange(args, 1, args.length));
                else sender.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
            else syntaxError(sender);
        } else syntaxError(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> possibilities = new ArrayList<>();

        if (args.length == 1)
            for (String sub : commands.keySet())
                if (sub.toLowerCase().startsWith(args[0].toLowerCase()))
                    possibilities.add(sub.toLowerCase());

        return possibilities.size() <= 0 ? null : possibilities;
    }
}
