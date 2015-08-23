package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.backend.bungee.BungeeServerInfo;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import io.puharesource.mc.titlemanager.commands.CommandParameter;
import io.puharesource.mc.titlemanager.commands.ParameterSupport;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import lombok.val;
import org.bukkit.command.CommandSender;

import java.util.Map;

@ParameterSupport(supportedParams = {"BUNGEE", "SILENT", "FADEIN", "STAY", "FADEOUT"})
public final class SubMessage extends TMSubCommand {
    public SubMessage() {
        super("msg", "titlemanager.command.message", "<player> <message>", "Sends a title message to the specified player, put inside of the message, to add a subtitle.", "message");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args, Map<String, CommandParameter> params) {
        if (args.length < 2) {
            syntaxError(sender);
            return;
        }

        BungeeServerInfo server = null;
        val silent = params.containsKey("SILENT");
        val config = TitleManager.getInstance().getConfigManager().getConfig();
        int fadeIn = config.welcomeMessageFadeIn;
        int stay = config.welcomeMessageStay;
        int fadeOut = config.welcomeMessageFadeOut;

        if(params.containsKey("BUNGEE")) {
            final CommandParameter param = params.get("BUNGEE");

            if (param.getValue() != null && !param.getValue().isEmpty()) {
                server = TitleManager.getInstance().getBungeeManager().getServers().get(param.getValue().toUpperCase());
            }
        }

        if (params.containsKey("FADEIN")) {
            CommandParameter param = params.get("FADEIN");
            if (param.getValue() != null) {
                try {
                    fadeIn = Integer.valueOf(param.getValue());
                } catch (NumberFormatException ignored) {}
            }
        }
        if (params.containsKey("STAY")) {
            CommandParameter param = params.get("STAY");
            if (param.getValue() != null) {
                try {
                    stay = Integer.valueOf(param.getValue());
                } catch (NumberFormatException ignored) {}
            }
        }
        if (params.containsKey("FADEOUT")) {
            CommandParameter param = params.get("FADEOUT");
            if (param.getValue() != null) {
                try {
                    fadeOut = Integer.valueOf(param.getValue());
                } catch (NumberFormatException ignored) {}
            }
        }

        val lines = MiscellaneousUtils.splitString(MiscellaneousUtils.combineArray(1, args));
        val object = MiscellaneousUtils.generateTitleObject(lines[0], lines[1], fadeIn, stay, fadeOut);

        val playerName = args[0];
        if (params.containsKey("BUNGEE")) {
            val manager = TitleManager.getInstance().getBungeeManager();
            val json = manager.getGson().toJson(object);

            if (server == null) {
                if (params.get("BUNGEE").getValue() == null) {
                    manager.broadcastBungeeMessage("TitleObject-Message", json, args[0]);

                    if (silent) return;

                    if (object instanceof IAnimation) {
                        sendSuccess(sender, "You have sent a bungeecord title animation broadcast");
                    } else {
                        final TitleObject title = (TitleObject) object;

                        if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                            sendSuccess(sender, "You have sent a bungeecord title with the message \"%s\" \"%s\" to \"%s\" in all servers", ((TitleObject) object).getTitle(), playerName);
                        } else {
                            sendSuccess(sender, "You have sent a bungeecord title with the message \"%s\" to \"%s\" in all servers", ((TitleObject) object).getTitle(), playerName);
                        }
                    }
                } else {
                    sendError(sender, "%s is an invalid server!", params.get("BUNGEE").getValue());
                }
            } else {
                server.sendMessage("TitleObject-Message", json, playerName);

                if (silent) return;

                if (object instanceof IAnimation) {
                    sendSuccess(sender, "You have sent a bungeecord title animation");
                } else {
                    final TitleObject title = (TitleObject) object;

                    if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                        sendSuccess(sender, "You have sent a bungeecord title with the message \"%s\" \"%s\" to %s in the server \"%s\"", title.getTitle(), title.getSubtitle(), playerName, server.getName());
                    } else {
                        sendSuccess(sender, "You have sent a bungeecord title with the message \"%s\" to %s in the server \"%s\"", title.getTitle(), playerName, server.getName());
                    }
                }
            }
        } else {
            val player = MiscellaneousUtils.getPlayer(playerName);
            if (player == null) {
                sendError(sender, "%s is not a player!", playerName);
                return;
            }

            object.send(player);

            if (silent) return;

            if (object instanceof IAnimation) {
                sendSuccess(sender, "You have sent a title animation to %s.", player.getName());
            } else {
                final TitleObject title = (TitleObject) object;

                if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                    sendSuccess(sender, "You have sent a title with the message \"%s\" \"%s\" to %s", title.getTitle(), title.getSubtitle(), player.getName());
                } else {
                    sendSuccess(sender, "You have sent a title with the message \"%s\" to %s", title.getTitle(), player.getName());
                }
            }
        }
    }
}
