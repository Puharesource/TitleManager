package io.puharesource.mc.sponge.titlemanager.commands.sub;

import com.google.common.base.Joiner;
import io.puharesource.mc.sponge.titlemanager.commands.CommandParameters;
import io.puharesource.mc.sponge.titlemanager.commands.TMSubCommand;
import lombok.val;
import org.spongepowered.api.text.format.TextColors;

import static io.puharesource.mc.sponge.titlemanager.Messages.*;

public final class SubAnimations extends TMSubCommand {
    private final Joiner joiner = Joiner.on(TextColors.WHITE + ", " + TextColors.GREEN);

    @Override
    public void onCommand(final CommandSender sender, final String[] args, final CommandParameters params) {
        val animations = Config.getAnimations().keySet();

        sendSuccess(sender, COMMAND_ANIMATIONS_SUCCESS, animations.size(), ChatColor.GREEN + joiner.join(animations));
    }
}
