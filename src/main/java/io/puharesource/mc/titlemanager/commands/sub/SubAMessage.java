package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SubAMessage extends TMSubCommand {
    public SubAMessage() {
        super("amsg", "titlemanager.command.amessage", "<player> <message>", "Sends an actionbar title message to the specified player.", "amessage");
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
        ActionbarTitleObject object = new ActionbarTitleObject(TitleManager.combineArray(1, args));
        object.send(player);
        sender.sendMessage(ChatColor.GREEN + "You have sent " + ChatColor.stripColor(player.getDisplayName()) + " \"" + ChatColor.RESET + object.getTitle() + ChatColor.GREEN + "\"");
    }
}
