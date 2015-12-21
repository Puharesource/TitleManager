package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.backend.bungee.BungeeServerInfo;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import io.puharesource.mc.titlemanager.commands.TMCommandException;
import io.puharesource.mc.titlemanager.commands.CommandParameters;
import io.puharesource.mc.titlemanager.commands.ParameterSupport;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import lombok.val;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static io.puharesource.mc.titlemanager.backend.language.Messages.*;

@ParameterSupport(supportedParams = {"SILENT", "WORLD", "BUNGEE", "RADIUS"})
public final class SubABroadcast extends TMSubCommand {
    public SubABroadcast() {
        super("abc", "titlemanager.command.abroadcast", COMMAND_ABROADCAST_USAGE, COMMAND_ABROADCAST_DESCRIPTION, "abroadcast");
    }

    @Override
    public void onCommand(final CommandSender sender, final String[] args, final CommandParameters params) throws TMCommandException {
        if (args.length < 1) {
            syntaxError(sender);
            return;
        }

        BungeeServerInfo server = null;

        if(params.contains("BUNGEE")) {
            val param = params.get("BUNGEE");

            if (param.getValue() != null && !param.getValue().isEmpty()) {
                server = TitleManager.getInstance().getBungeeManager().getServers().get(param.getValue());
            }
        }
        val silent = params.getBoolean("SILENT");
        val object = MiscellaneousUtils.generateActionbarObject(MiscellaneousUtils.combineArray(0, args));

        if (params.contains("WORLD")) {
            final World world = params.getWorld("WORLD");
            if (world == null) throw new TMCommandException(INVALID_WORLD);

            if(sender instanceof Player && (((Player) sender).getWorld().equals(world)) && params.contains("RADIUS")) {
                try {
                    for(final Player player : MiscellaneousUtils.getWithinRadius(((Player)sender).getLocation(), params.getDouble("RADIUS"))) {
                        object.send(player);
                    }
                } catch(NumberFormatException e) {
                    throw new TMCommandException(INVALID_RADIUS, params.get("RADIUS").getValue());
                }
            } else if (params.contains("RADIUS")) {
                throw new TMCommandException(WRONG_WORLD);
            } else {
                object.broadcast(world);
            }

            if (silent) return;

            if (object instanceof IAnimation) {
                sendSuccess(sender, COMMAND_ABROADCAST_WORLD_SUCCESS_ANIMATION, world.getName());
            } else {
                sendSuccess(sender, COMMAND_ABROADCAST_WORLD_SUCCESS, ((ActionbarTitleObject) object).getTitle(), world.getName());
            }
        } else if (params.getBoolean("BUNGEE")) {
            val manager = TitleManager.getInstance().getBungeeManager();
            val json = manager.getGson().toJson(object);

            if (server == null) {
                if (params.get("BUNGEE").getValue() == null) {
                    manager.broadcastBungeeMessage("ActionbarTitle-Broadcast", json);

                    if (silent) return;

                    if (object instanceof IAnimation) {
                        sendSuccess(sender, COMMAND_ABROADCAST_BUNGEECORD_SUCCESS);
                    } else {
                        sendSuccess(sender, COMMAND_ABROADCAST_BUNGEECORD_SUCCESS_ANIMATION, ((ActionbarTitleObject) object).getTitle());
                    }
                } else {
                    throw new TMCommandException(INVALID_SERVER, params.get("BUNGEE").getValue());
                }
            } else {
                server.sendMessage("ActionbarTitle-Broadcast", json);

                if (silent) return;

                if (object instanceof IAnimation) {
                    sendSuccess(sender, COMMAND_ABROADCAST_BUNGEECORD_SUCCESS_ANIMATION_TO_SERVER, server.getName());
                } else {
                    sendSuccess(sender, COMMAND_ABROADCAST_BUNGEECORD_SUCCESS_TO_SERVER, ((ActionbarTitleObject) object).getTitle(), server.getName());
                }
            }
        } else {
            if(sender instanceof Player && params.contains("RADIUS")) {
                try {
                    for(final Player player : MiscellaneousUtils.getWithinRadius(((Player) sender).getLocation(), params.getDouble("RADIUS"))) {
                        object.send(player);
                    }
                } catch(NumberFormatException e) {
                    throw new TMCommandException(INVALID_RADIUS, params.get("RADIUS").getValue());
                }
            } else {
                object.broadcast();
            }

            if (silent) return;

            if (object instanceof IAnimation) {
                sendSuccess(sender, COMMAND_ABROADCAST_BASIC_SUCCESS_ANIMATION);
            } else {
                sendSuccess(sender, COMMAND_ABROADCAST_BASIC_SUCCESS, ((ActionbarTitleObject) object).getTitle());
            }
        }
    }
}
