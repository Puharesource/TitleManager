package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SubABroadcast extends TMSubCommand {
    public SubABroadcast() {
        super("abc", "titlemanager.command.abroadcast", "<message>", "Sends an actionbar title message to everyone on the server.", "abroadcast");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            syntaxError(sender);
            return;
        }
        ActionbarTitleObject object = new ActionbarTitleObject(TitleManager.combineArray(0, args));
        for (Player player : Bukkit.getOnlinePlayers())
            object.send(player);
        sender.sendMessage(ChatColor.GREEN + "You have sent an actionbar broadcast with the message \"" + ChatColor.RESET + object.getTitle() + ChatColor.GREEN + "\"");
    }
}
