package io.puharesource.mc.sponge.titlemanager.commands.sub;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.commands.CommandParameters;
import io.puharesource.mc.sponge.titlemanager.commands.TMSubCommand;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.Set;

public final class SubScripts extends TMSubCommand {
    @Inject private TitleManager plugin;

    private final Joiner joiner = Joiner.on("&f, &a");

    @Override
    public void onCommand(final CommandSource source, final CommandContext args, final CommandParameters params) {
        final Set<String> scripts = plugin.getConfigHandler().getScripts().keySet();

        sendSuccess(source, plugin.getConfigHandler().getMessage("command.scripts.success", String.valueOf(scripts.size()), "&a" + joiner.join(scripts)));
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .permission("titlemanager.command.scripts")
                .description(Text.of(plugin.getConfigHandler().getMessage("command.scripts.description")))
                .extendedDescription(Text.of(plugin.getConfigHandler().getMessage("command.scripts.description_extended")))
                .arguments(GenericArguments.none())
                .executor(this)
                .build();
    }
}
