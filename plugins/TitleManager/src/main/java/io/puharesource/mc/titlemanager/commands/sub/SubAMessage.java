package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.backend.bungee.BungeeServerInfo;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import io.puharesource.mc.titlemanager.commands.CommandParameter;
import io.puharesource.mc.titlemanager.commands.ParameterSupport;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;

@ParameterSupport(supportedParams = {"BUNGEE", "SILENT"})
public final class SubAMessage extends TMSubCommand {
    public SubAMessage() {
        super("amsg", "titlemanager.command.amessage", "<player> <message>", "Sends an actionbar title message to the specified player.", "amessage");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args, Map<String, CommandParameter> params) {
        if (args.length < 2) {
            syntaxError(sender);
            return;
        }

        BungeeServerInfo server = null;
        val silent = params.containsKey("SILENT");
        val object = MiscellaneousUtils.generateActionbarObject(MiscellaneousUtils.combineArray(1, args));
        val playerName = args[0];

        if(params.containsKey("BUNGEE")) {
            val param = params.get("BUNGEE");

            if (param.getValue() != null && !param.getValue().isEmpty()) {
                server = TitleManager.getInstance().getBungeeManager().getServers().get(param.getValue().toUpperCase());
            }
        }

        if (params.containsKey("BUNGEE")) {
            val manager = TitleManager.getInstance().getBungeeManager();
            val json = manager.getGson().toJson(object);

            if (server == null) {
                if (params.get("BUNGEE").getValue() == null) {
                    manager.broadcastBungeeMessage("ActionbarTitle-Message", json, playerName);

                    if (silent) return;

                    if (object instanceof IAnimation)
                        sendSuccess(sender, "You have sent an actionbar animation to %s over BungeeCord.", playerName);
                    else sendSuccess(sender, "You have sent %s \"%s\" over BungeeCord", playerName, ((ActionbarTitleObject) object).getTitle());
                } else {
                    sendError(sender, "%s is an invalid server!", params.get("BUNGEE").getValue());
                }
            } else {
                server.sendMessage("ActionbarTitle-Message", json, playerName);

                if (silent) return;

                if (object instanceof IAnimation)
                    sendSuccess(sender, "You have sent an actionbar animation to %s in the server \"%s\".", playerName, server.getName());
                else sendSuccess(sender, "You have sent %s \"%s\" to the server \"%s\"", playerName, ((ActionbarTitleObject) object).getTitle(), server.getName());
            }
        } else {
            val player = MiscellaneousUtils.getPlayer(args[0]);
            if (player == null) {
                sendError(sender, "%s is not a currently online!", args[0]);
                return;
            }

            object.send(player);

            if (silent) return;

            if (object instanceof IAnimation)
                sendSuccess(sender, "You have sent an actionbar animation to %s.", ChatColor.stripColor(player.getDisplayName()));
            else sendSuccess(sender, "You have sent %s \"%s\"", ChatColor.stripColor(player.getDisplayName()), ((ActionbarTitleObject) object).getTitle());
        }
    }
}
