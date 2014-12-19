package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.api.animations.FrameSequence;
import io.puharesource.mc.titlemanager.api.animations.TitleAnimation;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
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

        if (args[0].toLowerCase().contains("<nl>") || args[0].toLowerCase().contains("{nl}")) {
            String[] lines = null;
            if (args[0].toLowerCase().contains("{nl}"))
                args[0] = args[0].replace("{nl}", "<nl>");
            if (args[0].toLowerCase().contains("<nl>"))
                lines = args[0].split("<nl>", 2);
            if (lines != null && (lines[0].toLowerCase().startsWith("animation:") || lines[1].toLowerCase().startsWith("animation:"))) {
                FrameSequence title = null;
                FrameSequence subtitle = null;

                if (lines[1].toLowerCase().startsWith("animation:")) {
                    String str = lines[1].substring("animation:".length());
                    title = Config.getAnimation(str);
                    if (title == null) {
                        sender.sendMessage(ChatColor.RED + str + " is an invalid animation!");
                        return;
                    }
                }
                if (lines[1].toLowerCase().startsWith("animation:")) {
                    String str = lines[1].substring("animation:".length());
                    subtitle = Config.getAnimation(str);
                    if (subtitle == null) {
                        sender.sendMessage(ChatColor.RED + str + " is an invalid animation!");
                        return;
                    }
                }
                new TitleAnimation(title == null ? lines[1] : title, subtitle == null ? lines[2] : subtitle).send(player);
                sender.sendMessage(ChatColor.GREEN + "You have sent a broadcast animation.");
                return;
            }
        } else if (args[1].toLowerCase().startsWith("animation:")) {
            String str = args[1].substring("animation:".length());
            FrameSequence animation = Config.getAnimation(str);
            if (animation != null) {
                new TitleAnimation(animation, "").send(player);
                sender.sendMessage(ChatColor.GREEN + "You have sent a broadcast animation.");
            } else sender.sendMessage(ChatColor.RED + str + " is an invalid animation!");
            return;
        }

        TitleObject object = TitleManager.generateTitleObjectFromArgs(1, args);
        object.send(player);
        if (object.getSubtitle() != null)
            sender.sendMessage(ChatColor.GREEN + "You have sent " + ChatColor.stripColor(player.getDisplayName()) + " \"" + ChatColor.RESET + object.getTitle() + ChatColor.GREEN + "\" \"" + ChatColor.RESET + object.getSubtitle() + ChatColor.GREEN + "\"");
        else
            sender.sendMessage(ChatColor.GREEN + "You have sent " + ChatColor.stripColor(player.getDisplayName()) + " \"" + ChatColor.RESET + object.getTitle() + ChatColor.GREEN + "\"");
    }
}
