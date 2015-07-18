package io.puharesource.mc.titlemanager.commands;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

public abstract class TMSubCommand {

    private @Getter final String alias;
    private @Getter final String node;
    private @Getter final String usage;
    private @Getter final String description;
    private @Getter final String[] aliases;

    private final Set<String> supportedParameters;

    public TMSubCommand(String alias, String node, String usage, String description, String... aliases) {
        this.alias = alias;
        this.node = node;
        this.usage = usage;
        this.description = description;
        this.aliases = aliases;
        this.supportedParameters = new HashSet<>();

        if (this.getClass().isAnnotationPresent(ParameterSupport.class)) {
            ParameterSupport parameterSupport = this.getClass().getAnnotation(ParameterSupport.class);

            for (String param : parameterSupport.supportedParams())
                supportedParameters.add(param.toUpperCase().trim());
        }
    }

    public abstract void onCommand(CommandSender sender, String[] args, Map<String, CommandParameter> params);

    public void syntaxError(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Wrong usage! Correct usage:");
        sender.sendMessage(ChatColor.RED + "   /" + getAlias() + " " + getUsage());
    }

    public Collection<String> getSupportedParameters() {
        return supportedParameters;
    }
}
