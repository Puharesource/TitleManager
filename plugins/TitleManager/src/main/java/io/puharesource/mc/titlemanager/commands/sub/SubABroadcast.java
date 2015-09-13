package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.backend.bungee.BungeeServerInfo;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import io.puharesource.mc.titlemanager.commands.CommandParameter;
import io.puharesource.mc.titlemanager.commands.CommandParameters;
import io.puharesource.mc.titlemanager.commands.ParameterSupport;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

@ParameterSupport(supportedParams = {"SILENT", "WORLD", "BUNGEE", "RADIUS"})
public final class SubABroadcast extends TMSubCommand {
    public SubABroadcast() {
        super("abc", "titlemanager.command.abroadcast", "<message>", "Sends an actionbar title message to everyone on the server.", "abroadcast");
    }

    @Override
    public void onCommand(final CommandSender sender, final String[] args, final CommandParameters params) {
        if (args.length < 1) {
            syntaxError(sender);
            return;
        }

        BungeeServerInfo server = null;

        if(params.contains("BUNGEE")) {
            val param = params.get("BUNGEE");

            if (param.getValue() != null && !param.getValue().isEmpty()) {
                server = TitleManager.getInstance().getBungeeManager().getServers().get(param.getValue().toUpperCase());
            }
        }
        val silent = params.getBoolean("SILENT");
        val object = MiscellaneousUtils.generateActionbarObject(MiscellaneousUtils.combineArray(0, args));

        if (params.contains("WORLD")) {
            final World world = params.getWorld("WORLD");

            if (world == null) {
                sendError(sender, "Invalid world!");
            } else {
                if(sender instanceof Player && (((Player) sender).getWorld().equals(world)) && params.contains("RADIUS")) {
                    try {
                        for(final Player player : MiscellaneousUtils.getWithinRadius(((Player)sender).getLocation(), params.getDouble("RADIUS"))) {
                            object.send(player);
                        }
                    } catch(NumberFormatException e) {
                        sendError(sender, "The radius specified is not valid! %s is not a valid number!", params.get("RADIUS").getValue());
                        return;
                    }
                } else if (params.contains("RADIUS")) {
                    sendError(sender, "You need to be in the world specified!");
                    return;
                } else {
                    object.broadcast(world);
                }

                if (silent) return;

                if (object instanceof IAnimation) {
                    sendSuccess(sender, "You have sent a world actionbar animation broadcast to the world \"%s\"", world.getName());
                } else {
                    sendSuccess(sender, "You have sent a world actionbar broadcast with the message \"%s\" to the world \"%s\"", ((ActionbarTitleObject) object).getTitle(), world.getName());
                }
            }
        } else if (params.getBoolean("BUNGEE")) {
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
            if(sender instanceof Player && params.contains("RADIUS")) {
                try {
                    for(final Player player : MiscellaneousUtils.getWithinRadius(((Player)sender).getLocation(), params.getDouble("RADIUS"))) {
                        object.send(player);
                    }
                } catch(NumberFormatException e) {
                    sendError(sender, "The radius specified is not valid! %s is not a valid number!", params.get("RADIUS").getValue());
                    return;
                }
            } else {
                object.broadcast();
            }

            if (silent) return;

            if (object instanceof IAnimation) {
                sendSuccess(sender, "You have sent an actionbar animation broadcast.");
            } else {
                sendSuccess(sender, "You have sent an actionbar broadcast with the message \"%s\"", ((ActionbarTitleObject) object).getTitle());
            }
        }
    }
}
