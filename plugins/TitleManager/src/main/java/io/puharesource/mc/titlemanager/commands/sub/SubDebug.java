package io.puharesource.mc.titlemanager.commands.sub;

import com.google.common.base.Joiner;
import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.api.animations.MultiFrameSequence;
import io.puharesource.mc.titlemanager.api.animations.TitleAnimation;
import io.puharesource.mc.titlemanager.backend.language.Messages;
import io.puharesource.mc.titlemanager.commands.CommandParameters;
import io.puharesource.mc.titlemanager.commands.TMCommandException;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.regex.Pattern;

public final class SubDebug extends TMSubCommand {
    public SubDebug() {
        super("debug", "Just for Puharesource", Messages.COMMAND_ABROADCAST_USAGE, Messages.COMMAND_ABROADCAST_DESCRIPTION, "test");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args, CommandParameters params) throws TMCommandException {
        if (!sender.getName().equals("Puharesource")) {
            sender.sendMessage(ChatColor.RED + "Sorry that I forgot about this command, please message me to remove this command from future versions of TitleManager.");
            return;
        }

        if (args.length == 2) {
            final Pattern pattern = Pattern.compile(args[0]);
            Bukkit.broadcastMessage(ChatColor.GREEN + "Pattern: " + ChatColor.WHITE + pattern.pattern());
            Bukkit.broadcastMessage(ChatColor.GREEN + "String: " + ChatColor.WHITE + args[1]);
            Bukkit.broadcastMessage(ChatColor.GREEN + "Matches: " + ChatColor.WHITE + pattern.matcher(args[1]).matches());
            Bukkit.broadcastMessage(ChatColor.GREEN + "Replaced: " + ChatColor.WHITE + args[1].replaceAll(pattern.pattern(), ""));
            Bukkit.broadcastMessage(ChatColor.GREEN + "Split: " + ChatColor.WHITE + Joiner.on(" ***** ").join(pattern.split(args[1])));
            Bukkit.broadcastMessage(ChatColor.STRIKETHROUGH + "--------------------------");

            return;
        }

        new TitleAnimation(new MultiFrameSequence(Arrays.asList(Config.getAnimation("left-to-right"), ChatColor.RESET + " Super Swag ", Config.getAnimation("right-to-left"))), "---").send((Player) sender);
    }
}
