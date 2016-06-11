package io.puharesource.mc.titlemanager.commands.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;
import io.puharesource.mc.titlemanager.backend.bungee.BungeeManager;
import io.puharesource.mc.titlemanager.backend.bungee.BungeeServerInfo;
import io.puharesource.mc.titlemanager.backend.config.ConfigMain;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import io.puharesource.mc.titlemanager.commands.CommandParameters;
import io.puharesource.mc.titlemanager.commands.ParameterSupport;
import io.puharesource.mc.titlemanager.commands.TMCommandException;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;

import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_MESSAGE_BASIC_SUCCESS_ANIMATION;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_MESSAGE_BASIC_SUCCESS_WITH_SUBTITLE;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_MESSAGE_BASIC_SUCCESS_WITH_TITLE;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_MESSAGE_BUNGEECORD_SUCCESS_ANIMATION;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_MESSAGE_BUNGEECORD_SUCCESS_ANIMATION_IN_SERVER;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_MESSAGE_BUNGEECORD_SUCCESS_WITH_SUBTITLE;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_MESSAGE_BUNGEECORD_SUCCESS_WITH_SUBTITLE_IN_SERVER;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_MESSAGE_BUNGEECORD_SUCCESS_WITH_TITLE;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_MESSAGE_BUNGEECORD_SUCCESS_WITH_TITLE_IN_SERVER;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_MESSAGE_DESCRIPTION;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_MESSAGE_USAGE;
import static io.puharesource.mc.titlemanager.backend.language.Messages.INVALID_PLAYER;
import static io.puharesource.mc.titlemanager.backend.language.Messages.INVALID_SERVER;
import static io.puharesource.mc.titlemanager.commands.CommandParameterIdentifier.BUNGEE;
import static io.puharesource.mc.titlemanager.commands.CommandParameterIdentifier.FADE_IN;
import static io.puharesource.mc.titlemanager.commands.CommandParameterIdentifier.FADE_OUT;
import static io.puharesource.mc.titlemanager.commands.CommandParameterIdentifier.SILENT;
import static io.puharesource.mc.titlemanager.commands.CommandParameterIdentifier.STAY;

@ParameterSupport(supportedParams = {SILENT, BUNGEE, FADE_IN, STAY, FADE_OUT})
public final class SubMessage extends TMSubCommand {
    public SubMessage() {
        super("msg", "titlemanager.command.message", COMMAND_MESSAGE_USAGE, COMMAND_MESSAGE_DESCRIPTION, "message");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args, final CommandParameters params) throws TMCommandException {
        if (args.length < 2) {
            syntaxError(sender);
            return;
        }

        final Optional<BungeeServerInfo> optionalServer = params.getServer(BUNGEE);
        final boolean silent = params.contains(SILENT);

        final ConfigMain config = TitleManager.getInstance().getConfigManager().getConfig();

        final int fadeIn = params.getInt(FADE_IN, config.welcomeMessageFadeIn);
        final int stay = params.getInt(STAY, config.welcomeMessageStay);
        final int fadeOut = params.getInt(FADE_OUT, config.welcomeMessageFadeOut);

        final String[] lines = MiscellaneousUtils.splitString(MiscellaneousUtils.combineArray(1, args));
        final ITitleObject titleObject = MiscellaneousUtils.generateTitleObject(lines[0], lines[1], fadeIn, stay, fadeOut);

        final String playerName = args[0];

        if (params.contains(BUNGEE)) {
            final BungeeManager manager = TitleManager.getInstance().getBungeeManager();
            final String json = manager.getGson().toJson(titleObject);

            if (optionalServer == null) {
                if (params.get(BUNGEE).getValue() == null) {
                    manager.broadcastBungeeMessage("TitleObject-Message", json, args[0]);

                    if (titleObject instanceof IAnimation) {
                        sendSuccess(silent, sender, COMMAND_MESSAGE_BUNGEECORD_SUCCESS_ANIMATION, playerName);
                    } else {
                        final TitleObject title = (TitleObject) titleObject;

                        if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                            sendSuccess(silent, sender, COMMAND_MESSAGE_BUNGEECORD_SUCCESS_WITH_SUBTITLE, ((TitleObject) titleObject).getTitle(), playerName);
                        } else {
                            sendSuccess(silent, sender, COMMAND_MESSAGE_BUNGEECORD_SUCCESS_WITH_TITLE, ((TitleObject) titleObject).getTitle(), playerName);
                        }
                    }
                } else {
                    throw new TMCommandException(INVALID_SERVER, params.get(BUNGEE).getValue());
                }
            } else {
                optionalServer.get().sendMessage("TitleObject-Message", json, playerName);

                if (titleObject instanceof IAnimation) {
                    sendSuccess(silent, sender, COMMAND_MESSAGE_BUNGEECORD_SUCCESS_ANIMATION_IN_SERVER, playerName, optionalServer.get().getName());
                } else {
                    final TitleObject title = (TitleObject) titleObject;

                    if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                        sendSuccess(silent, sender, COMMAND_MESSAGE_BUNGEECORD_SUCCESS_WITH_SUBTITLE_IN_SERVER, title.getTitle(), title.getSubtitle(), playerName, optionalServer.get().getName());
                    } else {
                        sendSuccess(silent, sender, COMMAND_MESSAGE_BUNGEECORD_SUCCESS_WITH_TITLE_IN_SERVER, title.getTitle(), playerName, optionalServer.get().getName());
                    }
                }
            }
        } else {
            final Player player = MiscellaneousUtils.getPlayer(playerName);
            if (player == null) {
                sendError(silent, sender, INVALID_PLAYER, playerName);
                return;
            }

            titleObject.send(player);

            if (titleObject instanceof IAnimation) {
                sendSuccess(silent, sender, COMMAND_MESSAGE_BASIC_SUCCESS_ANIMATION, player.getName());
            } else {
                final TitleObject title = (TitleObject) titleObject;

                if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                    sendSuccess(silent, sender, COMMAND_MESSAGE_BASIC_SUCCESS_WITH_SUBTITLE, title.getTitle(), title.getSubtitle(), player.getName());
                } else {
                    sendSuccess(silent, sender, COMMAND_MESSAGE_BASIC_SUCCESS_WITH_TITLE, title.getTitle(), player.getName());
                }
            }
        }
    }
}
