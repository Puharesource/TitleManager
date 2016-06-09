package io.puharesource.mc.titlemanager.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.backend.language.Messages;
import lombok.val;

public final class TMCommand implements CommandExecutor, TabCompleter {
    private final Map<String, TMSubCommand> commands = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public TMCommand() {
        val cmd = TitleManager.getInstance().getCommand("tm");

        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
    }

    public void addSubCommand(final TMSubCommand cmd) {
        commands.put(cmd.getAlias(), cmd);
        for (String alias : cmd.getAliases())
            commands.put(alias, cmd);
    }

    private void syntaxError(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + Messages.COMMAND_WRONG_USAGE.getMessage());
        final Set<String> aliases = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (TMSubCommand cmd : commands.values()) {
            if (aliases.contains(cmd.getAlias())) continue;
            sender.sendMessage(String.format(ChatColor.RED + Messages.COMMAND_WRONG_USAGE_TRAIL.getMessage(), "tm " + cmd.getAlias(), cmd.getUsage() + ChatColor.GRAY + (cmd.getUsage().isEmpty() ? "- " : " - ") + cmd.getDescription()));
            aliases.add(cmd.getAlias());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("tm")) return false;
        if (args.length >= 1) {
            final TMSubCommand subCommand = commands.get(args[0]);
            if (subCommand != null)
                if (sender.hasPermission(subCommand.getNode())) {
                    String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                    val parameters = getParameters(subCommand, subArgs);
                    try {
                        subCommand.onCommand(sender, Arrays.copyOfRange(subArgs, parameters.getParams().size(), subArgs.length), parameters);
                    } catch (TMCommandException e) {
                        sender.sendMessage(e.getMessage());
                    }
                } else sender.sendMessage(ChatColor.RED + Messages.COMMAND_NO_PERMISSION.getMessage());
            else syntaxError(sender);
        } else syntaxError(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> possibilities = new ArrayList<>();

        if (args.length == 1)
            possibilities.addAll(commands.keySet().stream()
                    .filter(sub -> sub.toLowerCase().startsWith(args[0].toLowerCase()))
                    .map(String::toLowerCase).collect(Collectors.toList()));

        return possibilities.size() <= 0 ? null : possibilities;
    }

    private CommandParameters getParameters(final TMSubCommand cmd, final String[] args) {
        final Collection<String> supportedParameters = cmd.getSupportedParameters();
        final Map<String, CommandParameter> parameters = new HashMap<>();

        for (String arg : args) {
            if (!arg.startsWith("-")) break;

            char[] chars = arg.toCharArray();
            String fullParameter = "";
            for (int i = 1; chars.length > i; i++) {
                fullParameter += chars[i];
            }

            if (fullParameter.contains("=")) {
                String[] paramValues = fullParameter.split("=", 2);
                String param = paramValues[0];

                if (supportedParameters.contains(param)) {
                    parameters.put(param, new CommandParameter(param, paramValues[1]));
                } else break;
            } else {
                if (supportedParameters.contains(fullParameter)) {
                    parameters.put(fullParameter, new CommandParameter(fullParameter, null));
                } else break;
            }
        }

        return new CommandParameters(parameters);
    }
}
