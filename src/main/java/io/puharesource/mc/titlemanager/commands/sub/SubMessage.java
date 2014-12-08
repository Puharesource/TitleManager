package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TitleObject;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SubMessage extends TMSubCommand {
    public SubMessage() {
        super("msg", "titlemanager.command.message", "<player> <message>", "Sends a title message to the specified player, put inside of the message, to add a subtitle.", "message");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            syntaxError(sender);
            return;
        }

        Player player = TitleManager.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + args[0] + " is not a player!");
            return;
        }
        TitleObject object = TitleManager.generateTitleObjectFromArgs(1, args);
        object.send(player);
        if (object.getSubtitle() != null)
            sender.sendMessage(ChatColor.GREEN + "You have sent " + ChatColor.stripColor(player.getDisplayName()) + " \"" + ChatColor.RESET + object.getTitle() + ChatColor.GREEN + "\" \"" + ChatColor.RESET + object.getSubtitle() + ChatColor.GREEN + "\"");
        else sender.sendMessage(ChatColor.GREEN + "You have sent " + ChatColor.stripColor(player.getDisplayName()) + " \"" + ChatColor.RESET + object.getTitle() + ChatColor.GREEN + "\"");
    }
}
