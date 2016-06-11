package io.puharesource.mc.titlemanager.commands.sub;

import org.bukkit.command.CommandSender;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.commands.CommandParameters;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;

import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_VERSION_DESCRIPTION;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_VERSION_SUCCESS;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_VERSION_USAGE;

public final class SubVersion extends TMSubCommand {
    public SubVersion() {
        super("version", "titlemanager.command.version", COMMAND_VERSION_USAGE, COMMAND_VERSION_DESCRIPTION);
    }

    @Override
    public void onCommand(final CommandSender sender, final String[] args, final CommandParameters params) {
        sendSuccess(false, sender, COMMAND_VERSION_SUCCESS, TitleManager.getInstance().getDescription().getFullName());
    }
}
