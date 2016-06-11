package io.puharesource.mc.titlemanager.commands.sub;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.commands.CommandParameters;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;

import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_RELOAD_DESCRIPTION;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_RELOAD_SUCCESS;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_RELOAD_SUCCESS_HOVERING;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_RELOAD_USAGE;

public final class SubReload extends TMSubCommand {
    public SubReload() {
        super("reload", "titlemanager.command.reload", COMMAND_RELOAD_USAGE, COMMAND_RELOAD_DESCRIPTION);
    }

    @Override
    public void onCommand(final CommandSender sender, final String[] args, final CommandParameters params) {
        TitleManager.getInstance().getConfigManager().reload();
        sendSuccess(false, sender, COMMAND_RELOAD_SUCCESS);

        if (sender instanceof Player) {
            new TitleObject(
                    ChatColor.GREEN + COMMAND_RELOAD_SUCCESS_HOVERING.getMessage(),
                    TitleObject.TitleType.TITLE)
                    .setFadeIn(10)
                    .setStay(15)
                    .setFadeOut(10)
                    .send((Player) sender);
        }
    }
}
