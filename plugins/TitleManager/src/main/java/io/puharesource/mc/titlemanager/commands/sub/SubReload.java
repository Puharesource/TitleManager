package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SubReload extends TMSubCommand {
    public SubReload() {
        super("reload", "titlemanager.command.reload", "", "Reloads the config.");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        TitleManager.getInstance().getConfigManager().reload();
        sender.sendMessage(ChatColor.GREEN + "The configuration has been reloaded.");
        if (sender instanceof Player)
            new TitleObject(ChatColor.GREEN + "Config Reloaded!", TitleObject.TitleType.TITLE).setFadeIn(10).setStay(15).setFadeOut(10).send((Player) sender);
    }
}
