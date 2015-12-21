package io.puharesource.mc.titlemanager.commands;

import io.puharesource.mc.titlemanager.backend.language.Messages;
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

    public TMSubCommand(final String alias, final String node, final Messages usage, final Messages description, final String... aliases) {
        this.alias = alias;
        this.node = node;
        this.usage = usage.getMessage();
        this.description = description.getMessage();
        this.aliases = aliases;
        this.supportedParameters = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        if (this.getClass().isAnnotationPresent(ParameterSupport.class)) {
            ParameterSupport parameterSupport = this.getClass().getAnnotation(ParameterSupport.class);
            Collections.addAll(supportedParameters, parameterSupport.supportedParams());
        }
    }

    public abstract void onCommand(final CommandSender sender, final String[] args, final CommandParameters params) throws TMCommandException;

    public void syntaxError(final CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Wrong usage! Correct usage:");
        sender.sendMessage(ChatColor.RED + "   /" + getAlias() + " " + getUsage());
    }

    public Collection<String> getSupportedParameters() {
        return supportedParameters;
    }

    private void sendFormattedMessage(final CommandSender sender, final ChatColor color, final Messages message, final Object... args) {
        sender.sendMessage(color + String.format(message.getMessage().replace("%s", ChatColor.RESET + "%s" + color), args));
    }

    protected void sendSuccess(final CommandSender sender, final Messages message, final Object... args) {
        sendFormattedMessage(sender, ChatColor.GREEN, message, args);
    }

    protected void sendError(final CommandSender sender, final Messages message, final Object... args) {
        sendFormattedMessage(sender, ChatColor.RED, message, args);
    }
}
