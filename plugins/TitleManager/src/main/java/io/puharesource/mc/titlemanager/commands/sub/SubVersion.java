package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.commands.CommandParameter;
import io.puharesource.mc.titlemanager.commands.CommandParameters;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class SubVersion extends TMSubCommand {
    public SubVersion() {
        super("version", "titlemanager.command.version", "", "Tells you the version of TitleManager that's running on your server.");
    }

    @Override
    public void onCommand(final CommandSender sender, final String[] args, final CommandParameters params) {
        sendSuccess(sender, "This server is running %s.", TitleManager.getInstance().getDescription().getFullName());
    }
}
