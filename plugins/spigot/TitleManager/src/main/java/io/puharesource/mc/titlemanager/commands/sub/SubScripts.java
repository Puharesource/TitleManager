package io.puharesource.mc.titlemanager.commands.sub;

import com.google.common.base.Joiner;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Set;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.commands.CommandParameters;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;

import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_SCRIPTS_DESCRIPTION;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_SCRIPTS_SUCCESS;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_SCRIPTS_USAGE;

public final class SubScripts extends TMSubCommand {
    private final Joiner joiner = Joiner.on(ChatColor.WHITE + ", " + ChatColor.GREEN);

    public SubScripts() {
        super("scripts", "titlemanager.command.scripts", COMMAND_SCRIPTS_USAGE, COMMAND_SCRIPTS_DESCRIPTION, "scriptslist", "scriptlist");
    }

    @Override
    public void onCommand(final CommandSender sender, final String[] args, final CommandParameters params) {
        final Set<String> scripts = TitleManager.getInstance().getConfigManager().getScripts().keySet();

        sendSuccess(false, sender, COMMAND_SCRIPTS_SUCCESS, scripts.size(), ChatColor.GREEN + joiner.join(scripts));
    }
}
