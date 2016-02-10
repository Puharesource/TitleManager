package io.puharesource.mc.sponge.titlemanager.commands.sub;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.TitlePosition;
import io.puharesource.mc.sponge.titlemanager.api.TitleObject;
import io.puharesource.mc.sponge.titlemanager.commands.CommandParameters;
import io.puharesource.mc.sponge.titlemanager.commands.TMSubCommand;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import static io.puharesource.mc.sponge.titlemanager.Messages.COMMAND_RELOAD_SUCCESS;
import static io.puharesource.mc.sponge.titlemanager.Messages.COMMAND_RELOAD_SUCCESS_HOVERING;

public final class SubReload extends TMSubCommand {
    @Inject private TitleManager plugin;

    @Override
    public void onCommand(final CommandSource source, final CommandContext args, final CommandParameters params) {
        plugin.getConfigHandler().reload();
        sendSuccess(source, COMMAND_RELOAD_SUCCESS);
        if (source instanceof Player)
            new TitleObject(TextColors.GREEN + COMMAND_RELOAD_SUCCESS_HOVERING.getMessage(), TitlePosition.TITLE).setFadeIn(10).setStay(15).setFadeOut(10).send((Player) source);
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .permission("titlemanager.command.reload")
                .description(Text.of("Reloads the configuration file."))
                .arguments(GenericArguments.none())
                .executor(this)
                .build();
    }
}
