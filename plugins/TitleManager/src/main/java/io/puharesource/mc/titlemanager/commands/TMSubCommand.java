package io.puharesource.mc.titlemanager.commands;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class TMSubCommand {

    private @Getter final String alias;
    private @Getter final String node;
    private @Getter final String usage;
    private @Getter final String description;
    private @Getter final String[] aliases;

    private final Set<String> supportedParameters;

    public TMSubCommand(final String alias, final String node, final String usage, final String description, final String... aliases) {
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

    public abstract void onCommand(final CommandSender sender, final String[] args, final Map<String, CommandParameter> params);

    public void syntaxError(final CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Wrong usage! Correct usage:");
        sender.sendMessage(ChatColor.RED + "   /" + getAlias() + " " + getUsage());
    }

    public Collection<String> getSupportedParameters() {
        return supportedParameters;
    }

    private void sendFormattedMessage(final CommandSender sender, final ChatColor color, final String message, final Object... args) {
        sender.sendMessage(color + String.format(message.replace("%s", ChatColor.RESET + "%s" + color), args));
    }

    protected void sendSuccess(final CommandSender sender, final String message, final Object... args) {
        sendFormattedMessage(sender, ChatColor.GREEN, message, args);
    }

    protected void sendError(final CommandSender sender, final String message, final Object... args) {
        sendFormattedMessage(sender, ChatColor.RED, message, args);
    }
}
