package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.commands.CommandParameter;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class SubVersion extends TMSubCommand {
    public SubVersion() {
        super("version", "titlemanager.command.version", "", "Tells you the version of TitleManager that's running on your server.");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args, Map<String, CommandParameter> params) {
        sender.sendMessage(ChatColor.GREEN + "This server is running " + TitleManager.getInstance().getDescription().getFullName() + ".");
    }
}
