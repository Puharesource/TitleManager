package io.puharesource.mc.titlemanager.commands.sub;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.backend.bungee.BungeeManager;
import io.puharesource.mc.titlemanager.backend.bungee.BungeeServerInfo;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import io.puharesource.mc.titlemanager.commands.CommandParameters;
import io.puharesource.mc.titlemanager.commands.ParameterSupport;
import io.puharesource.mc.titlemanager.commands.TMCommandException;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;

import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_AMESSAGE_BASIC_SUCCESS;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_AMESSAGE_BASIC_SUCCESS_ANIMATION;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_AMESSAGE_BUNGEECORD_SUCCESS;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_AMESSAGE_BUNGEECORD_SUCCESS_ANIMATION;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_AMESSAGE_BUNGEECORD_SUCCESS_ANIMATION_IN_SERVER;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_AMESSAGE_BUNGEECORD_SUCCESS_IN_SERVER;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_AMESSAGE_DESCRIPTION;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_AMESSAGE_USAGE;
import static io.puharesource.mc.titlemanager.backend.language.Messages.INVALID_PLAYER;
import static io.puharesource.mc.titlemanager.commands.CommandParameterIdentifier.BUNGEE;
import static io.puharesource.mc.titlemanager.commands.CommandParameterIdentifier.SILENT;

@ParameterSupport(supportedParams = {SILENT, BUNGEE})
public final class SubAMessage extends TMSubCommand {
    public SubAMessage() {
        super("amsg", "titlemanager.command.amessage", COMMAND_AMESSAGE_USAGE, COMMAND_AMESSAGE_DESCRIPTION, "amessage");
    }

    @Override
    public void onCommand(final CommandSender sender, final String[] args, final CommandParameters params) throws TMCommandException {
        if (args.length < 2) {
            syntaxError(sender);
            return;
        }

        final Optional<BungeeServerInfo> optionalServer = params.getServer(BUNGEE);
        final boolean silent = params.contains(SILENT);
        final IActionbarObject actionbarObject = MiscellaneousUtils.generateActionbarObject(MiscellaneousUtils.combineArray(1, args));
        final String playerName = args[0];

        if (params.contains(BUNGEE)) {
            final BungeeManager manager = TitleManager.getInstance().getBungeeManager();
            final String json = manager.getGson().toJson(actionbarObject);

            if (optionalServer.isPresent()) {
                final BungeeServerInfo server = optionalServer.get();
                server.sendMessage("ActionbarTitle-Message", json, playerName);

                if (actionbarObject instanceof IAnimation){
                    sendSuccess(silent, sender, COMMAND_AMESSAGE_BUNGEECORD_SUCCESS_ANIMATION_IN_SERVER, playerName, optionalServer.get());
                } else {
                    sendSuccess(silent, sender, COMMAND_AMESSAGE_BUNGEECORD_SUCCESS_IN_SERVER, playerName, ((ActionbarTitleObject) actionbarObject).getTitle(), optionalServer.get());
                }
            } else {
                manager.broadcastBungeeMessage("ActionbarTitle-Message", json, playerName);

                if (actionbarObject instanceof IAnimation)
                    sendSuccess(silent, sender, COMMAND_AMESSAGE_BUNGEECORD_SUCCESS_ANIMATION, playerName);
                else sendSuccess(silent, sender, COMMAND_AMESSAGE_BUNGEECORD_SUCCESS, playerName, ((ActionbarTitleObject) actionbarObject).getTitle());
            }
        } else {
            final Player player = MiscellaneousUtils.getPlayer(args[0]);
            if (player == null) throw new TMCommandException(INVALID_PLAYER, args[0]);

            actionbarObject.send(player);

            if (actionbarObject instanceof IAnimation) {
                sendSuccess(silent, sender, COMMAND_AMESSAGE_BASIC_SUCCESS, ChatColor.stripColor(player.getDisplayName()));
            } else {
                sendSuccess(silent, sender, COMMAND_AMESSAGE_BASIC_SUCCESS_ANIMATION, ChatColor.stripColor(player.getDisplayName()), ((ActionbarTitleObject) actionbarObject).getTitle());
            }
        }
    }
}
