package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.commands.CommandParameters;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import static io.puharesource.mc.titlemanager.backend.language.Messages.*;

public final class SubAnimations extends TMSubCommand {
    public SubAnimations() {
        super("animations", "titlemanager.command.animations", COMMAND_ANIMATIONS_USAGE, COMMAND_ANIMATIONS_DESCRIPTION, "animationslist", "animationlist");
    }

    @Override
    public void onCommand(final CommandSender sender, final String[] args, final CommandParameters params) {
        StringBuilder sb = new StringBuilder();
        String[] animations = Config.getAnimations().keySet().toArray(new String[Config.getAnimations().size()]);
        for (int i = 0; animations.length > i; i++)
            sb.append(i == 0 ? ChatColor.GREEN : ChatColor.WHITE + ", " + ChatColor.GREEN).append(animations[i].toLowerCase().trim());
        sendSuccess(sender, COMMAND_ANIMATIONS_SUCCESS, animations.length, sb.toString());
    }
}
