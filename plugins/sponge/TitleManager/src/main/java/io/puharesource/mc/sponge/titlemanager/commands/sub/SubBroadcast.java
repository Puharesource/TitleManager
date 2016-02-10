package io.puharesource.mc.sponge.titlemanager.commands.sub;

import io.puharesource.mc.sponge.titlemanager.commands.*;
import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;
import io.puharesource.mc.titlemanager.backend.bungee.BungeeServerInfo;
import io.puharesource.mc.titlemanager.backend.config.ConfigMain;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import lombok.val;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static io.puharesource.mc.titlemanager.backend.language.Messages.*;

@ParameterSupport(supportedParams = {})
public final class SubBroadcast extends TMSubCommand {
    public SubBroadcast() {
        super("SILENT", "FADEIN", "STAY", "FADEOUT", "WORLD", "RADIUS");
    }

    @Override
    public void onCommand(final CommandSender sender, final String[] args, final CommandParameters params) throws TMCommandException {
        if (args.length < 1) {
            syntaxError(sender);
            return;
        }

        BungeeServerInfo server = null;
        final boolean silent = params.getBoolean("SILENT");
        final ConfigMain config = TitleManager.getInstance().getConfigManager().getConfig();

        int fadeIn = params.getInt("FADEIN", config.welcomeMessageFadeIn);
        int stay = params.getInt("STAY", config.welcomeMessageStay);
        int fadeOut = params.getInt("FADEOUT", config.welcomeMessageFadeOut);
        val world = params.getWorld("WORLD");

        if(params.contains("BUNGEE")) {
            final CommandParameter param = params.get("BUNGEE");

            if (param.getValue() != null && !param.getValue().isEmpty()) {
                server = TitleManager.getInstance().getBungeeManager().getServers().get(param.getValue());
            }
        }

        final String[] lines = MiscellaneousUtils.splitString(MiscellaneousUtils.combineArray(0, args));
        final ITitleObject object = MiscellaneousUtils.generateTitleObject(lines[0], lines[1], fadeIn, stay, fadeOut);

        if (params.contains("WORLD")) {//No support for radius if world != own world
            if (world == null) throw new TMCommandException(INVALID_WORLD, params.get("WORLD").getValue());

            if(sender instanceof Player && (((Player) sender).getWorld().equals(world)) && params.contains("RADIUS") && params.get("RADIUS").getValue() != null) {
                try {
                    for(final Player player : MiscellaneousUtils.getWithinRadius(((Player)sender).getLocation(), Integer.parseInt(params.get("RADIUS").getValue()))) {
                        object.send(player);
                    }
                } catch(NumberFormatException e) {
                    throw new TMCommandException(INVALID_RADIUS, params.get("RADIUS").getValue());
                }
            } else if (params.contains("RADIUS") && params.get("RADIUS").getValue() != null) {
                throw new TMCommandException(WRONG_WORLD);
            } else {
                object.broadcast(world);
            }

            if (silent) return;

            if (object instanceof IAnimation) {
                sendSuccess(sender, COMMAND_BROADCAST_WORLD_SUCCESS_ANIMATION, world.getName());
            } else {
                final TitleObject title = (TitleObject) object;

                if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                    sendSuccess(sender, COMMAND_BROADCAST_WORLD_SUCCESS_WITH_SUBTITLE, ((TitleObject) object).getTitle(), world.getName());
                } else {
                    sendSuccess(sender, COMMAND_BROADCAST_WORLD_SUCCESS_WITH_TITLE, ((TitleObject) object).getTitle(), world.getName());
                }
            }
        } else if (params.contains("BUNGEE")) {
            val manager = TitleManager.getInstance().getBungeeManager();
            val json = manager.getGson().toJson(object);

            if (server == null) {
                if (params.get("BUNGEE").getValue() == null) {
                    manager.broadcastBungeeMessage("TitleObject-Broadcast", json);

                    if (silent) return;

                    if (object instanceof IAnimation) {
                        sendSuccess(sender, COMMAND_BROADCAST_BUNGEECORD_SUCCESS_ANIMATION);
                    } else {
                        final TitleObject title = (TitleObject) object;

                        if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                            sendSuccess(sender, COMMAND_BROADCAST_BUNGEECORD_SUCCESS_WITH_Subtitle, ((TitleObject) object).getTitle());
                        } else {
                            sendSuccess(sender, COMMAND_BROADCAST_BUNGEECORD_SUCCESS_WITH_Title, ((TitleObject) object).getTitle());
                        }
                    }
                } else {
                    throw new TMCommandException(INVALID_SERVER, params.get("BUNGEE").getValue());
                }
            } else {
                server.sendMessage("TitleObject-Broadcast", json);

                if (silent) return;

                if (object instanceof IAnimation) {
                    sendSuccess(sender, COMMAND_BROADCAST_BUNGEECORD_SUCCESS_ANIMATION_TO_SERVER);
                } else {
                    final TitleObject title = (TitleObject) object;

                    if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                        sendSuccess(sender, COMMAND_BROADCAST_BUNGEECORD_SUCCESS_WITH_SUBTITLE_TO_SERVER, title.getTitle(), title.getSubtitle(), server.getName());
                    } else {
                        sendSuccess(sender, COMMAND_BROADCAST_BUNGEECORD_SUCCESS_WITH_TITLE_TO_SERVER, title.getTitle(), server.getName());
                    }
                }
            }
        } else {
            if(sender instanceof Player && params.contains("RADIUS")) {
                try {
                    for(final Player player : MiscellaneousUtils.getWithinRadius(((Player)sender).getLocation(), Integer.parseInt(params.get("RADIUS").getValue()))) {
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
                sendSuccess(sender, COMMAND_BROADCAST_BASIC_SUCCESS_ANIMATION);
            } else {
                final TitleObject title = (TitleObject) object;

                if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                    sendSuccess(sender, COMMAND_BROADCAST_BASIC_SUCCESS_WITH_SUBTITLE, title.getTitle(), title.getSubtitle());
                } else {
                    sendSuccess(sender, COMMAND_BROADCAST_BASIC_SUCCESS_WITH_TITLE, title.getTitle());
                }
            }
        }
    }
}
