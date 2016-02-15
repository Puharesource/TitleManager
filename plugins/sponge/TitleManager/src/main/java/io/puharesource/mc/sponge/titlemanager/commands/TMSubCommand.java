package io.puharesource.mc.sponge.titlemanager.commands;

import com.google.common.collect.ImmutableSet;
import io.puharesource.mc.sponge.titlemanager.Messages;
import io.puharesource.mc.sponge.titlemanager.MiscellaneousUtils;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.parsing.InputTokenizer;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public abstract class TMSubCommand implements CommandExecutor {
    private final Set<String> supportedParameters = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    public TMSubCommand(final String... parameters) {
        this.supportedParameters.addAll(Arrays.stream(parameters).collect(Collectors.toSet()));
    }

    public InputTokenizer createTokenizer() {
        return null; //TODO: Make this actually work.
    }

    public abstract CommandSpec createSpec();

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

    protected void sendSuccess(final CommandSource source, final String message) {
        source.sendMessage(Text.of(TextColors.GREEN, MiscellaneousUtils.format(message)));
    }

    protected void sendError(final CommandSource source, final String message) {
        source.sendMessage(Text.of(TextColors.RED, MiscellaneousUtils.format(message)));
    }
}
