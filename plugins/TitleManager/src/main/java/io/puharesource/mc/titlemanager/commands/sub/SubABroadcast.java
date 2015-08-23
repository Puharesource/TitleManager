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
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Map;

@ParameterSupport(supportedParams = {"SILENT", "WORLD", "BUNGEE"})
public final class SubABroadcast extends TMSubCommand {
    public SubABroadcast() {
        super("abc", "titlemanager.command.abroadcast", "<message>", "Sends an actionbar title message to everyone on the server.", "abroadcast");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args, Map<String, CommandParameter> params) {
        if (args.length < 1) {
            syntaxError(sender);
            return;
        }

        BungeeServerInfo server = null;

        if(params.containsKey("BUNGEE")) {
            val param = params.get("BUNGEE");

            if (param.getValue() != null && !param.getValue().isEmpty()) {
                server = TitleManager.getInstance().getBungeeManager().getServers().get(param.getValue().toUpperCase());
            }
        }

        World world = null;

        val silent = params.containsKey("SILENT");
        val object = MiscellaneousUtils.generateActionbarObject(MiscellaneousUtils.combineArray(0, args));

        if (params.containsKey("WORLD")) {
            CommandParameter param = params.get("WORLD");
            if (param.getValue() != null) {
                world = Bukkit.getWorld(param.getValue());
            }
        }

        if (params.containsKey("WORLD")) {
            if (world == null) {
                sendError(sender, "Invalid world!");
            } else {
                for (val player : world.getPlayers()) {
                    object.send(player);
                }

                if (silent) return;

                if (object instanceof IAnimation) {
                    sendSuccess(sender, "You have sent a world actionbar animation broadcast to the world \"%s\"", world.getName());
                } else {
                    sendSuccess(sender, "You have sent a world actionbar broadcast with the message \"%s\" to the world \"%s\"", ((ActionbarTitleObject) object).getTitle(), world.getName());
                }
            }
        } else if (params.containsKey("BUNGEE")) {
            val manager = TitleManager.getInstance().getBungeeManager();
            val json = manager.getGson().toJson(object);

            if (server == null) {
                if (params.get("BUNGEE").getValue() == null) {
                    manager.broadcastBungeeMessage("ActionbarTitle-Broadcast", json);

                    if (silent) return;

                    if (object instanceof IAnimation) {
                        sendSuccess(sender, "You have sent a bungeecord actionbar animation broadcast");
                    } else {
                        sendSuccess(sender, "You have sent a bungeecord actionbar broadcast with the message \"%s\"", ((ActionbarTitleObject) object).getTitle());
                    }
                } else {
                    sendError(sender, "%s is an invalid server!", params.get("BUNGEE").getValue());
                }
            } else {
                server.sendMessage("ActionbarTitle-Broadcast", json);

                if (silent) return;

                if (object instanceof IAnimation) {
                    sendSuccess(sender, "You have sent a bungeecord actionbar animation broadcast to the server \"%s\"", server.getName());
                } else {
                    sendSuccess(sender, "You have sent a bungeecord actionbar broadcast with the message \"%s\" to the server \"%s\"", ((ActionbarTitleObject) object).getTitle(), server.getName());
                }
            }
        } else {
            object.broadcast();

            if (silent) return;

            if (object instanceof IAnimation) {
                sendSuccess(sender, "You have sent an actionbar animation broadcast.");
            } else {
                sendSuccess(sender, "You have sent an actionbar broadcast with the message \"%s\"", ((ActionbarTitleObject) object).getTitle());
            }
        }
    }
}
