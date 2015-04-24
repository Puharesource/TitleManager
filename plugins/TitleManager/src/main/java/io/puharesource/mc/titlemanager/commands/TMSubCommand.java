package io.puharesource.mc.titlemanager.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TMSubCommand {

    private final String alias;
    private final String node;
    private final String usage;
    private final String description;
    private final String[] aliases;

    private final List<String> supportedParameters;

    public TMSubCommand(String alias, String node, String usage, String description, String... aliases) {
        this.alias = alias;
        this.node = node;
        this.usage = usage;
        this.description = description;
        this.aliases = aliases;
        this.supportedParameters = new ArrayList<>();

        if (this.getClass().isAnnotationPresent(ParameterSupport.class)) {
            ParameterSupport parameterSupport = this.getClass().getAnnotation(ParameterSupport.class);

            for (String param : parameterSupport.supportedParams())
                supportedParameters.add(param.toUpperCase().trim());
        }
    }

    public abstract void onCommand(CommandSender sender, String[] args, String[] params);

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

    public Map<String, CommandParameter> getParameters(String[] args) {
        Map<String, CommandParameter> parameters = new HashMap<>();

        for (String arg : args) {
            if (!arg.startsWith("-")) break;

            char[] chars = arg.toCharArray();
            String fullParameter = "";
            for (int i = 1; chars.length > i; i++) {
                fullParameter += chars[i];
            }

            if (fullParameter.contains("=")) {
                String[] paramValues = fullParameter.split("=", 2);

                if (!supportedParameters.contains(paramValues[0])) {

                }
            }
        }
    }
}
