package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;


public class SubAnimations extends TMSubCommand {
    public SubAnimations() {
        super("animations", "titlemanager.command.animations", "", "Lists you all of the animations.", "animationslist");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        StringBuilder sb = new StringBuilder();
        String[] animations = Config.getAnimations().keySet().toArray(new String[Config.getAnimations().size()]);
        for (int i = 0; animations.length > i; i++)
            sb.append(i == 0 ? ChatColor.GREEN : ChatColor.WHITE + ", " + ChatColor.GREEN).append(animations[i].toLowerCase().trim());
        sender.sendMessage(String.format(ChatColor.GREEN + "Loaded animations (%s): %s", ChatColor.WHITE.toString() + animations.length + ChatColor.GREEN, sb.toString()));
    }
}
