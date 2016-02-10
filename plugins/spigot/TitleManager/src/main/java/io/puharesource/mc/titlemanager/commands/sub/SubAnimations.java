package io.puharesource.mc.titlemanager.commands.sub;

import com.google.common.base.Joiner;
import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.commands.CommandParameters;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import static io.puharesource.mc.titlemanager.backend.language.Messages.*;

public final class SubAnimations extends TMSubCommand {
    private final Joiner joiner = Joiner.on(ChatColor.WHITE + ", " + ChatColor.GREEN);

    public SubAnimations() {
        super("animations", "titlemanager.command.animations", COMMAND_ANIMATIONS_USAGE, COMMAND_ANIMATIONS_DESCRIPTION, "animationslist", "animationlist");
    }

    @Override
    public void onCommand(final CommandSender sender, final String[] args, final CommandParameters params) {
        val animations = Config.getAnimations().keySet();

        sendSuccess(sender, COMMAND_ANIMATIONS_SUCCESS, animations.size(), ChatColor.GREEN + joiner.join(animations));
    }
}
