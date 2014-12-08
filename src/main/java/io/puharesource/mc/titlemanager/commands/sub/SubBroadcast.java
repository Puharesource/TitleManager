package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TitleObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SubBroadcast extends TMSubCommand {
    public SubBroadcast() {
        super("bc", "titlemanager.command.broadcast", "<message>", "Sends a title message to everyone on the server, put inside of the message, to add a subtitle.", "broadcast");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            syntaxError(sender);
            return;
        }

        TitleObject object = TitleManager.generateTitleObjectFromArgs(0, args);
        for (Player player : Bukkit.getOnlinePlayers())
            object.send(player);
        if (object.getSubtitle() != null)
            sender.sendMessage(ChatColor.GREEN + "You have sent a broadcast with the message \"" + ChatColor.RESET + object.getTitle() + ChatColor.GREEN + "\" \"" + ChatColor.RESET + object.getSubtitle() + ChatColor.GREEN + "\"");
        else
            sender.sendMessage(ChatColor.GREEN + "You have sent a broadcast with the message \"" + ChatColor.RESET + object.getTitle() + ChatColor.GREEN + "\"");
    }
}
