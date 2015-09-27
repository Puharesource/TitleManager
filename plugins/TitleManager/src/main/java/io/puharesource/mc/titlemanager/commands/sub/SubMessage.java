package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;

import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import io.puharesource.mc.titlemanager.commands.CommandParameters;
import io.puharesource.mc.titlemanager.commands.ParameterSupport;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import lombok.val;
import org.bukkit.command.CommandSender;

import static io.puharesource.mc.titlemanager.backend.language.Messages.*;

@ParameterSupport(supportedParams = {"BUNGEE", "SILENT", "FADEIN", "STAY", "FADEOUT"})
public final class SubMessage extends TMSubCommand {
    public SubMessage() {
        super("msg", "titlemanager.command.message", COMMAND_MESSAGE_USAGE, COMMAND_MESSAGE_DESCRIPTION, "message");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args, final CommandParameters params) {
        if (args.length < 2) {
            syntaxError(sender);
            return;
        }

        val server = params.getServer("BUNGEE");
        val silent = params.getBoolean("SILENT");
        val config = TitleManager.getInstance().getConfigManager().getConfig();
        int fadeIn = params.getInt("FADEIN", config.welcomeMessageFadeIn);
        int stay = params.getInt("STAY", config.welcomeMessageStay);
        int fadeOut = params.getInt("FADEOUT", config.welcomeMessageFadeOut);

        val lines = MiscellaneousUtils.splitString(MiscellaneousUtils.combineArray(1, args));
        val object = MiscellaneousUtils.generateTitleObject(lines[0], lines[1], fadeIn, stay, fadeOut);

        val playerName = args[0];
        if (params.getBoolean("BUNGEE")) {
            val manager = TitleManager.getInstance().getBungeeManager();
            val json = manager.getGson().toJson(object);

            if (server == null) {
                if (params.get("BUNGEE").getValue() == null) {
                    manager.broadcastBungeeMessage("TitleObject-Message", json, args[0]);

                    if (silent) return;

                    if (object instanceof IAnimation) {
                        sendSuccess(sender, COMMAND_MESSAGE_BUNGEECORD_SUCCESS_ANIMATION, playerName);
                    } else {
                        final TitleObject title = (TitleObject) object;

                        if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                            sendSuccess(sender, COMMAND_MESSAGE_BUNGEECORD_SUCCESS_WITH_SUBTITLE, ((TitleObject) object).getTitle(), playerName);
                        } else {
                            sendSuccess(sender, COMMAND_MESSAGE_BUNGEECORD_SUCCESS_WITH_TITLE, ((TitleObject) object).getTitle(), playerName);
                        }
                    }
                } else {
                    sendError(sender, INVALID_SERVER, params.get("BUNGEE").getValue());
                }
            } else {
                server.sendMessage("TitleObject-Message", json, playerName);

                if (silent) return;

                if (object instanceof IAnimation) {
                    sendSuccess(sender, COMMAND_MESSAGE_BUNGEECORD_SUCCESS_ANIMATION_IN_SERVER, playerName, server.getName());
                } else {
                    final TitleObject title = (TitleObject) object;

                    if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                        sendSuccess(sender, COMMAND_MESSAGE_BUNGEECORD_SUCCESS_WITH_SUBTITLE_IN_SERVER, title.getTitle(), title.getSubtitle(), playerName, server.getName());
                    } else {
                        sendSuccess(sender, COMMAND_MESSAGE_BUNGEECORD_SUCCESS_WITH_TITLE_IN_SERVER, title.getTitle(), playerName, server.getName());
                    }
                }
            }
        } else {
            val player = MiscellaneousUtils.getPlayer(playerName);
            if (player == null) {
                sendError(sender, INVALID_PLAYER, playerName);
                return;
            }

            object.send(player);

            if (silent) return;

            if (object instanceof IAnimation) {
                sendSuccess(sender, COMMAND_MESSAGE_BASIC_SUCCESS_ANIMATION, player.getName());
            } else {
                final TitleObject title = (TitleObject) object;

                if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                    sendSuccess(sender, COMMAND_MESSAGE_BASIC_SUCCESS_WITH_SUBTITLE, title.getTitle(), title.getSubtitle(), player.getName());
                } else {
                    sendSuccess(sender, COMMAND_MESSAGE_BASIC_SUCCESS_WITH_TITLE, title.getTitle(), player.getName());
                }
            }
        }
    }
}
