package io.puharesource.mc.sponge.titlemanager.commands;

import io.puharesource.mc.sponge.titlemanager.commands.sub.*;
import lombok.Getter;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

public final class TMCommand implements CommandExecutor {
    @Getter private final CommandSpec commandSpec;
    @Getter private Logger logger;

    public TMCommand() {
        logger.debug("Registering commands.");

        // /tm abroadcast
        logger.debug("Building command: /tm abroadcast");
        final CommandSpec subABroadcast = CommandSpec.builder()
                .permission("titlemanager.command.abroadcast")
                .description(Text.of())
                .executor(new SubABroadcast())
                .build();

        // /tm amessage
        logger.debug("Building command: /tm amessage");
        final CommandSpec subAMessage = CommandSpec.builder()
                .permission("titlemanager.command.amessage")
                .description(Text.of())
                .executor(new SubAMessage())
                .build();

        // /tm animations
        logger.debug("Building command: /tm animations");
        final CommandSpec subAnimations = CommandSpec.builder()
                .permission("titlemanager.command.animations")
                .description(Text.of())
                .executor(new SubAnimations())
                .build();

        // /tm broadcast
        logger.debug("Building command: /tm broadcast");
        final CommandSpec subBroadcast = CommandSpec.builder()
                .permission("titlemanager.command.broadcast")
                .description(Text.of())
                .executor(new SubBroadcast())
                .build();

        // /tm message
        logger.debug("Building command: /tm message");
        final CommandSpec subMessage = CommandSpec.builder()
                .permission("titlemanager.command.message")
                .description(Text.of())
                .executor(new SubMessage())
                .build();

        // /tm reload
        logger.debug("Building command: /tm reload");
        final CommandSpec subReload = CommandSpec.builder()
                .permission("titlemanager.command.reload")
                .description(Text.of())
                .executor(new SubReload())
                .build();

        // /tm scripts
        logger.debug("Building command: /tm scripts");
        final CommandSpec subScripts = CommandSpec.builder()
                .permission("titlemanager.command.scripts")
                .description(Text.of())
                .executor(new SubScripts())
                .build();

        // /tm version
        logger.debug("Building command: /tm version");
        final CommandSpec subVersion = CommandSpec.builder()
                .permission("titlemanager.command.version")
                .description(Text.of())
                .executor(new SubVersion())
                .build();

        // /tm
        logger.debug("Building main command. /tm");
        final CommandSpec mainCommand = CommandSpec.builder()
                .description(Text.of("TitleManager's main command."))
                .executor(new TMCommand())
                .child(subABroadcast)
                .child(subABroadcast)
                .child(subAnimations)
                .child(subBroadcast)
                .child(subMessage)
                .child(subReload)
                .child(subScripts)
                .child(subVersion)
                .build();

        logger.debug("Registering command /tm.");
        Sponge.getCommandManager().register(this, mainCommand, "tm", "titlemanager");

        logger.debug("Finished registering commands.");
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
