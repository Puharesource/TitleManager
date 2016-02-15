package io.puharesource.mc.sponge.titlemanager.commands.sub;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.commands.CommandParameters;
import io.puharesource.mc.sponge.titlemanager.commands.TMSubCommand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public final class SubVersion extends TMSubCommand {
    @Inject private TitleManager plugin;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .permission("titlemanager.command.version")
                .description(Text.of(plugin.getConfigHandler().getMessage("command.version.description")))
                .extendedDescription(Text.of(plugin.getConfigHandler().getMessage("command.version.description_extended")))
                .executor(this)
                .build();
    }

    @Override
    public void onCommand(final CommandSource source, final CommandContext args, final CommandParameters params) {
        final Optional<PluginContainer> oContainer = Sponge.getPluginManager().fromInstance(plugin);

        if (!oContainer.isPresent()) {
            source.sendMessage(Text.of(TextColors.RED + "Something went wrong!"));
            return;
        }

        final PluginContainer container = oContainer.get();

        sendSuccess(source, plugin.getConfigHandler().getMessage("command.version.success", container.getName() + " version: " + container.getVersion()));
    }
}
