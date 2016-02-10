package io.puharesource.mc.sponge.titlemanager.commands.sub;

import com.google.common.base.Joiner;
import io.puharesource.mc.sponge.titlemanager.commands.CommandParameters;
import io.puharesource.mc.sponge.titlemanager.commands.TMSubCommand;
import io.puharesource.mc.titlemanager.TitleManager;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import static io.puharesource.mc.titlemanager.backend.language.Messages.*;

public final class SubScripts extends TMSubCommand {
    private final Joiner joiner = Joiner.on(ChatColor.WHITE + ", " + ChatColor.GREEN);

    public SubScripts() {
        super("scripts", "titlemanager.command.scripts", COMMAND_SCRIPTS_USAGE, COMMAND_SCRIPTS_DESCRIPTION, "scriptslist", "scriptlist");
    }

    @Override
    public void onCommand(final CommandSender sender, final String[] args, final CommandParameters params) {
        val scripts = TitleManager.getInstance().getConfigManager().getScripts().keySet();

        sendSuccess(sender, COMMAND_SCRIPTS_SUCCESS, scripts.size(), ChatColor.GREEN + joiner.join(scripts));
    }
}
