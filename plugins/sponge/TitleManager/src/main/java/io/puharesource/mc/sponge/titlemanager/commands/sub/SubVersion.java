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

import static io.puharesource.mc.sponge.titlemanager.Messages.COMMAND_VERSION_SUCCESS;

public final class SubVersion extends TMSubCommand {
    @Inject private TitleManager plugin;

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .permission("titlemanager.command.version")
                .description(Text.of("Tells you the version of the plugin."))
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

        sendSuccess(source, COMMAND_VERSION_SUCCESS, container.getName() + " version: " + container.getVersion());
    }
}
