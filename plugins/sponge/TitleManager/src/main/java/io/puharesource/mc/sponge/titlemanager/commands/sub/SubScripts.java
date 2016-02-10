package io.puharesource.mc.sponge.titlemanager.commands.sub;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.commands.CommandParameters;
import io.puharesource.mc.sponge.titlemanager.commands.TMSubCommand;
import lombok.val;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import static io.puharesource.mc.sponge.titlemanager.Messages.COMMAND_SCRIPTS_SUCCESS;

public final class SubScripts extends TMSubCommand {
    @Inject private TitleManager plugin;

    private final Joiner joiner = Joiner.on(TextColors.WHITE + ", " + TextColors.GREEN);

    @Override
    public void onCommand(final CommandSource source, final CommandContext args, final CommandParameters params) {
        val scripts = plugin.getConfigHandler().getScripts().keySet();

        sendSuccess(source, COMMAND_SCRIPTS_SUCCESS, scripts.size(), TextColors.GREEN + joiner.join(scripts));
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .permission("titlemanager.command.scripts")
                .description(Text.of("Lists all scripts."))
                .extendedDescription(Text.of("Lists all currently loaded scripts."))
                .arguments(GenericArguments.none())
                .executor(this)
                .build();
    }
}
