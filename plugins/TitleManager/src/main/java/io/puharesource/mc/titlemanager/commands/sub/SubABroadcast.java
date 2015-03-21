package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import io.puharesource.mc.titlemanager.api.animations.ActionbarTitleAnimation;
import io.puharesource.mc.titlemanager.api.animations.FrameSequence;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

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
        if (args[0].toLowerCase().startsWith("animation:")) {
            String str = args[0].substring("animation:".length());
            FrameSequence sequence = Config.getAnimation(str);
            if (sequence != null) {
                new ActionbarTitleAnimation(sequence).broadcast();
                sender.sendMessage(ChatColor.GREEN + "You have sent an actionbar animation broadcast.");
            } else sender.sendMessage(ChatColor.RED + str + " is an invalid animation!");
            return;
        }
        ActionbarTitleObject object = new ActionbarTitleObject(MiscellaneousUtils.combineArray(0, args));
        object.broadcast();
        sender.sendMessage(ChatColor.GREEN + "You have sent an actionbar broadcast with the message \"" + ChatColor.RESET + object.getTitle() + ChatColor.GREEN + "\"");
    }
}
