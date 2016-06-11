package io.puharesource.mc.titlemanager.commands.sub;

import org.bukkit.World;
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

import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_ABROADCAST_BASIC_SUCCESS;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_ABROADCAST_BASIC_SUCCESS_ANIMATION;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_ABROADCAST_BUNGEECORD_SUCCESS;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_ABROADCAST_BUNGEECORD_SUCCESS_ANIMATION;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_ABROADCAST_BUNGEECORD_SUCCESS_ANIMATION_TO_SERVER;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_ABROADCAST_BUNGEECORD_SUCCESS_TO_SERVER;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_ABROADCAST_DESCRIPTION;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_ABROADCAST_USAGE;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_ABROADCAST_WORLD_SUCCESS;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_ABROADCAST_WORLD_SUCCESS_ANIMATION;
import static io.puharesource.mc.titlemanager.backend.language.Messages.INVALID_RADIUS;
import static io.puharesource.mc.titlemanager.backend.language.Messages.WRONG_WORLD;
import static io.puharesource.mc.titlemanager.commands.CommandParameterIdentifier.BUNGEE;
import static io.puharesource.mc.titlemanager.commands.CommandParameterIdentifier.RADIUS;
import static io.puharesource.mc.titlemanager.commands.CommandParameterIdentifier.SILENT;
import static io.puharesource.mc.titlemanager.commands.CommandParameterIdentifier.WORLD;

@ParameterSupport(supportedParams = {SILENT, WORLD, BUNGEE, RADIUS})
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

        final Optional<BungeeServerInfo> optionalServer = params.getServer(BUNGEE);
        final boolean silent = params.containsValue(SILENT);
        final IActionbarObject object = MiscellaneousUtils.generateActionbarObject(MiscellaneousUtils.combineArray(0, args));

        if (params.contains(WORLD)) {
            final Optional<World> worldOptional = params.getWorld(WORLD);

            final World world;
            if (!worldOptional.isPresent() && sender instanceof Player) {
                world = worldOptional.orElse(((Player) sender).getWorld());
            } else {
                world = worldOptional.get();
            }

            if (sender instanceof Player) {
                final World playerWorld = ((Player) sender).getWorld();

                if (world.equals(playerWorld)) {
                    if (params.contains(RADIUS)) {
                        final Optional<Double> radiusOptional = params.getDouble(RADIUS);

                        if (radiusOptional.isPresent()) {
                            MiscellaneousUtils
                                    .getWithinRadius(((Player) sender).getLocation(), radiusOptional.get())
                                    .forEach(object::send);
                        } else if (radiusOptional.isPresent()) {
                            throw new TMCommandException(INVALID_RADIUS, params.get(RADIUS).getValue());
                        }
                    } else {
                        throw new TMCommandException(WRONG_WORLD);
                    }
                }
            } else {
                worldOptional.ifPresent(object::broadcast);
            }

            if (object instanceof IAnimation) {
                sendSuccess(silent, sender, COMMAND_ABROADCAST_WORLD_SUCCESS_ANIMATION, world.getName());
            } else {
                sendSuccess(silent, sender, COMMAND_ABROADCAST_WORLD_SUCCESS, ((ActionbarTitleObject) object).getTitle(), world.getName());
            }
        } else if (params.contains(BUNGEE)) {
            final BungeeManager manager = TitleManager.getInstance().getBungeeManager();
            final String json = manager.getGson().toJson(object);

            if (optionalServer.isPresent()) {
                final BungeeServerInfo server = optionalServer.get();

                server.sendMessage("ActionbarTitle-Broadcast", json);

                if (object instanceof IAnimation) {
                    sendSuccess(silent, sender, COMMAND_ABROADCAST_BUNGEECORD_SUCCESS_ANIMATION_TO_SERVER, server.getName());
                } else {
                    sendSuccess(silent, sender, COMMAND_ABROADCAST_BUNGEECORD_SUCCESS_TO_SERVER, ((ActionbarTitleObject) object).getTitle(), server.getName());
                }
            } else {
                manager.broadcastBungeeMessage("ActionbarTitle-Broadcast", json);

                if (object instanceof IAnimation) {
                    sendSuccess(silent, sender, COMMAND_ABROADCAST_BUNGEECORD_SUCCESS);
                } else {
                    sendSuccess(silent, sender, COMMAND_ABROADCAST_BUNGEECORD_SUCCESS_ANIMATION, ((ActionbarTitleObject) object).getTitle());
                }
            }
        } else {
            if(sender instanceof Player && params.containsValue(RADIUS)) {
                final Optional<Double> optionalRadius = params.getDouble(RADIUS);

                if (optionalRadius.isPresent()) {
                    MiscellaneousUtils
                            .getWithinRadius(((Player) sender).getLocation(), optionalRadius.get())
                            .forEach(object::send);
                } else {
                    throw new TMCommandException(INVALID_RADIUS, params.get(RADIUS).getValue());
                }
            } else {
                object.broadcast();
            }

            if (object instanceof IAnimation) {
                sendSuccess(silent, sender, COMMAND_ABROADCAST_BASIC_SUCCESS_ANIMATION);
            } else {
                sendSuccess(silent, sender, COMMAND_ABROADCAST_BASIC_SUCCESS, ((ActionbarTitleObject) object).getTitle());
            }
        }
    }
}
