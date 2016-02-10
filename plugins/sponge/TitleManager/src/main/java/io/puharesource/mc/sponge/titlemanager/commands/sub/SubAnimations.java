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
import org.spongepowered.api.text.format.TextColors;

import java.util.Set;

import static io.puharesource.mc.sponge.titlemanager.Messages.COMMAND_ANIMATIONS_SUCCESS;

public final class SubAnimations extends TMSubCommand {
    @Inject private TitleManager plugin;

    private final Joiner joiner = Joiner.on(TextColors.WHITE + ", " + TextColors.GREEN);

    @Override
    public void onCommand(final CommandSource source, final CommandContext args, final CommandParameters params) {
        final Set<String> animations = plugin.getConfigHandler().getAnimations().keySet();

        sendSuccess(source, COMMAND_ANIMATIONS_SUCCESS, animations.size(), TextColors.GREEN + joiner.join(animations));
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .permission("titlemanager.command.animations")
                .description(Text.of("Lists all animations."))
                .extendedDescription(Text.of("Lists all currently loaded animations."))
                .arguments(GenericArguments.none())
                .executor(this)
                .build();
    }
}
