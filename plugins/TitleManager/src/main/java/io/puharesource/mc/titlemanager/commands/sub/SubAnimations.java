package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.commands.CommandParameter;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class SubAnimations extends TMSubCommand {
    public SubAnimations() {
        super("animations", "titlemanager.command.animations", "", "Lists you all of the animations.", "animationslist", "animationlist");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args, Map<String, CommandParameter> params) {
        StringBuilder sb = new StringBuilder();
        String[] animations = Config.getAnimations().keySet().toArray(new String[Config.getAnimations().size()]);
        for (int i = 0; animations.length > i; i++)
            sb.append(i == 0 ? ChatColor.GREEN : ChatColor.WHITE + ", " + ChatColor.GREEN).append(animations[i].toLowerCase().trim());
        sendSuccess(sender, "Available animations (%s): %s", animations.length, sb.toString());
    }
}
