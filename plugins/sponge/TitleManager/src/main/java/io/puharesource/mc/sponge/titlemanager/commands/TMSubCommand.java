package io.puharesource.mc.sponge.titlemanager.commands;

import com.google.common.collect.ImmutableSet;
import io.puharesource.mc.sponge.titlemanager.Messages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public abstract class TMSubCommand implements CommandExecutor {
    private final Set<String> supportedParameters;

    public TMSubCommand(final String... parameters) {
        this.supportedParameters = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        this.supportedParameters.addAll(Arrays.stream(parameters).collect(Collectors.toSet()));
    }

    @Override
    @NonnullByDefault
    public CommandResult execute(final CommandSource source, final CommandContext args) throws CommandException {
        try {
            onCommand(source, args, null);
        } catch (TMCommandException e) {
            source.sendMessage(Text.of(e.getMessage()));
        }
        return CommandResult.success();
    }

    public abstract void onCommand(final CommandSource source, final CommandContext args, final CommandParameters params) throws TMCommandException;

    public void syntaxError(final CommandSource source) {
        source.sendMessage(Text.of(TextColors.RED + "Wrong usage! Correct usage:"));
        source.sendMessage(Text.of(TextColors.RED + "   /" + getAlias() + " " + getUsage()));
    }

    public Set<String> getSupportedParameters() {
        return ImmutableSet.copyOf(supportedParameters);
    }

    private void sendFormattedMessage(final CommandSource source, final TextColor color, final Messages message, final Object... args) {
        source.sendMessage(Text.of(color + String.format(message.getMessage().replace("%s", TextColors.RESET + "%s" + color), args)));
    }

    protected void sendSuccess(final CommandSource source, final Messages message, final Object... args) {
        sendFormattedMessage(source, TextColors.GREEN, message, args);
    }

    protected void sendError(final CommandSource source, final Messages message, final Object... args) {
        sendFormattedMessage(source, TextColors.RED, message, args);
    }
}
