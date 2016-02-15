package io.puharesource.mc.sponge.titlemanager.commands.sub;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.api.Sendables;
import io.puharesource.mc.sponge.titlemanager.api.TitlePosition;
import io.puharesource.mc.sponge.titlemanager.commands.CommandParameters;
import io.puharesource.mc.sponge.titlemanager.commands.TMSubCommand;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public final class SubReload extends TMSubCommand {
    @Inject private TitleManager plugin;

    @Override
    public void onCommand(final CommandSource source, final CommandContext args, final CommandParameters params) {
        plugin.getConfigHandler().reload();
        sendSuccess(source, plugin.getConfigHandler().getMessage("command.reload.success"));

        if (source instanceof Player) {
            Sendables.title(Text.of(TextColors.GREEN, plugin.getConfigHandler().getMessage("command.reload.success_title")), TitlePosition.TITLE).setFadeIn(10).setStay(15).setFadeOut(10).send((Player) source);
        }
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .permission("titlemanager.command.reload")
                .description(Text.of(plugin.getConfigHandler().getMessage("command.reload.description")))
                .extendedDescription(Text.of(plugin.getConfigHandler().getMessage("command.reload.description_extended")))
                .arguments(GenericArguments.none())
                .executor(this)
                .build();
    }
}
