package io.puharesource.mc.sponge.titlemanager.commands;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.commands.sub.*;
import lombok.Getter;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class TMCommand implements CommandExecutor {
    @Getter private CommandSpec commandSpec;
    @Getter @Inject private Logger logger;
    @Inject private TitleManager plugin;

    private Set<CommandSpec> children = new HashSet<>();

    public void load() {
        logger.debug("Registering commands.");

        // /tm
        logger.debug("Building main command. /tm");
        final CommandSpec.Builder mainCommandBuilder = CommandSpec.builder()
                .description(Text.of("TitleManager's main command."))
                .arguments(GenericArguments.none())
                .executor(new TMCommand());

        registerChild(mainCommandBuilder, new SubABroadcast(), "abc");
        registerChild(mainCommandBuilder, new SubAMessage(), "amsg");
        registerChild(mainCommandBuilder, new SubAnimations(), "animations");
        registerChild(mainCommandBuilder, new SubBroadcast(), "bc");
        registerChild(mainCommandBuilder, new SubMessage(), "msg");
        registerChild(mainCommandBuilder, new SubReload(), "reload");
        registerChild(mainCommandBuilder, new SubScripts(), "scripts");
        registerChild(mainCommandBuilder, new SubVersion(), "version");

        logger.debug("Registering command /tm.");
        this.commandSpec = mainCommandBuilder.build();
        Sponge.getCommandManager().register(plugin, this.commandSpec, "tm", "titlemanager");

        logger.debug("Finished registering commands.");
    }

    private void registerChild(final CommandSpec.Builder rootBuilder, final TMSubCommand sub, final String cmd) {
        plugin.getInjector().injectMembers(sub);

        logger.debug("Constructing sub command: " + cmd);
        final CommandSpec spec = sub.createSpec();
        logger.debug("Finished construction of sub command: " + cmd);

        logger.debug("Registering sub command: " + cmd);
        rootBuilder.child(spec);
        children.add(spec);
        logger.debug("Finished registering sub command: " + cmd);
    }

    private void syntaxError(final CommandSource source) {
        final Optional<? extends CommandMapping> oMainCommand = Sponge.getCommandManager().get("tm");

        if (!oMainCommand.isPresent()) {
            source.sendMessage(Text.of(TextColors.RED + "The /tm is not initialized correctly!"));
            return;
        }

        final CommandMapping mainCommand = oMainCommand.get();
        final Text fallBack = Text.of(TextColors.RED + "Wrong usage!");
        final Optional<? extends Text> helpText = mainCommand.getCallable().getHelp(source);

        if (helpText.isPresent()) {
            source.sendMessage(helpText.get().toText());
        } else {
            source.sendMessage(fallBack);
        }
    }

    @Override
    @NonnullByDefault
    public CommandResult execute(final CommandSource source, final CommandContext args) throws CommandException {
        syntaxError(source);
        return CommandResult.success();
    }
}
