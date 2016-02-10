package io.puharesource.mc.sponge.titlemanager.commands.sub;

import io.puharesource.mc.sponge.titlemanager.commands.CommandParameters;
import io.puharesource.mc.sponge.titlemanager.commands.TMSubCommand;
import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TitleObject;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static io.puharesource.mc.titlemanager.backend.language.Messages.*;

public final class SubReload extends TMSubCommand {
    public SubReload() {
        super("reload", "titlemanager.command.reload", COMMAND_RELOAD_USAGE, COMMAND_RELOAD_DESCRIPTION);
    }

    @Override
    public void onCommand(final CommandSender sender, final String[] args, final CommandParameters params) {
        TitleManager.getInstance().getConfigManager().reload();
        sendSuccess(sender, COMMAND_RELOAD_SUCCESS);
        if (sender instanceof Player)
            new TitleObject(ChatColor.GREEN + COMMAND_RELOAD_SUCCESS_HOVERING.getMessage(), TitleObject.TitleType.TITLE).setFadeIn(10).setStay(15).setFadeOut(10).send((Player) sender);
    }
}
