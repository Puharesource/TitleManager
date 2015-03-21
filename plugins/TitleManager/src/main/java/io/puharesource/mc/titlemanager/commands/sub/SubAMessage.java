package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import io.puharesource.mc.titlemanager.api.animations.ActionbarTitleAnimation;
import io.puharesource.mc.titlemanager.api.animations.FrameSequence;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
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

        Player player = MiscellaneousUtils.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + args[0] + " is not a player!");
            return;
        }

        if (args[1].toLowerCase().startsWith("animation:")) {
            String str = args[1].substring("animation:".length());
            FrameSequence sequence = Config.getAnimation(str);
            if (sequence != null) {
                new ActionbarTitleAnimation(sequence).send(player);
                sender.sendMessage(ChatColor.GREEN + "You have sent an actionbar animation to " + player.getName() + ".");
            } else sender.sendMessage(ChatColor.RED + str + " is an invalid animation!");
            return;
        }

        ActionbarTitleObject object = new ActionbarTitleObject(MiscellaneousUtils.combineArray(1, args));
        object.send(player);
        sender.sendMessage(ChatColor.GREEN + "You have sent " + ChatColor.stripColor(player.getDisplayName()) + " \"" + ChatColor.RESET + object.getTitle() + ChatColor.GREEN + "\"");
    }
}
