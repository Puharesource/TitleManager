package io.puharesource.mc.sponge.titlemanager.commands;

import com.google.common.collect.ImmutableSet;
import io.puharesource.mc.sponge.titlemanager.utils.MiscellaneousUtils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.parsing.InputTokenizer;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.*;
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
            final Map<String, CommandParameter> params = new HashMap<>();

            supportedParameters.stream().forEach(param -> params.put(param, new CommandParameter(param, null)));

            onCommand(source, args, new CommandParameters(params));
        } catch (TMCommandException e) {
            source.sendMessage(Text.of(e.getMessage()));
        }
        return CommandResult.success();
    }

    public abstract void onCommand(final CommandSource source, final CommandContext args, final CommandParameters params) throws TMCommandException;

    public void syntaxError(final CommandSource source) {
        source.sendMessage(Text.of(TextColors.RED, "Wrong usage! Correct usage:"));
        source.sendMessage(Text.of(TextColors.RED, "   /" + "" + " " + ""));
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
