package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.commands.CommandParameter;
import io.puharesource.mc.titlemanager.commands.CommandParameters;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public final class SubReload extends TMSubCommand {
    public SubReload() {
        super("reload", "titlemanager.command.reload", "", "Reloads the config.");
    }

    @Override
    public void onCommand(final CommandSender sender, final String[] args, final CommandParameters params) {
        TitleManager.getInstance().getConfigManager().reload();
        sendSuccess(sender, "The configuration has been reloaded.");
        if (sender instanceof Player)
            new TitleObject(ChatColor.GREEN + "Config Reloaded!", TitleObject.TitleType.TITLE).setFadeIn(10).setStay(15).setFadeOut(10).send((Player) sender);
    }
}
