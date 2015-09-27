package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import io.puharesource.mc.titlemanager.commands.CommandParameters;
import io.puharesource.mc.titlemanager.commands.ParameterSupport;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import static io.puharesource.mc.titlemanager.backend.language.Messages.*;

@ParameterSupport(supportedParams = {"BUNGEE", "SILENT"})
public final class SubAMessage extends TMSubCommand {
    public SubAMessage() {
        super("amsg", "titlemanager.command.amessage", COMMAND_AMESSAGE_USAGE, COMMAND_AMESSAGE_DESCRIPTION, "amessage");
    }

    @Override
    public void onCommand(final CommandSender sender, final String[] args, final CommandParameters params) {
        if (args.length < 2) {
            syntaxError(sender);
            return;
        }

        val server = params.getServer("BUNGEE");
        val silent = params.getBoolean("SILENT");
        val object = MiscellaneousUtils.generateActionbarObject(MiscellaneousUtils.combineArray(1, args));
        val playerName = args[0];

        if (params.contains("BUNGEE")) {
            val manager = TitleManager.getInstance().getBungeeManager();
            val json = manager.getGson().toJson(object);

            if (server == null) {
                if (params.get("BUNGEE").getValue() == null) {
                    manager.broadcastBungeeMessage("ActionbarTitle-Message", json, playerName);

                    if (silent) return;

                    if (object instanceof IAnimation)
                        sendSuccess(sender, COMMAND_AMESSAGE_BUNGEECORD_SUCCESS_ANIMATION, playerName);
                    else sendSuccess(sender, COMMAND_AMESSAGE_BUNGEECORD_SUCCESS, playerName, ((ActionbarTitleObject) object).getTitle());
                } else {
                    sendError(sender, INVALID_SERVER, params.get("BUNGEE").getValue());
                }
            } else {
                server.sendMessage("ActionbarTitle-Message", json, playerName);

                if (silent) return;

                if (object instanceof IAnimation)
                    sendSuccess(sender, COMMAND_AMESSAGE_BUNGEECORD_SUCCESS_ANIMATION_IN_SERVER, playerName, server.getName());
                else sendSuccess(sender, COMMAND_AMESSAGE_BUNGEECORD_SUCCESS_IN_SERVER, playerName, ((ActionbarTitleObject) object).getTitle(), server.getName());
            }
        } else {
            val player = MiscellaneousUtils.getPlayer(args[0]);
            if (player == null) {
                sendError(sender, INVALID_PLAYER, args[0]);
                return;
            }

            object.send(player);

            if (silent) return;

            if (object instanceof IAnimation)
                sendSuccess(sender, COMMAND_AMESSAGE_BASIC_SUCCESS, ChatColor.stripColor(player.getDisplayName()));
            else sendSuccess(sender, COMMAND_AMESSAGE_BASIC_SUCCESS_ANIMATION, ChatColor.stripColor(player.getDisplayName()), ((ActionbarTitleObject) object).getTitle());
        }
    }
}
