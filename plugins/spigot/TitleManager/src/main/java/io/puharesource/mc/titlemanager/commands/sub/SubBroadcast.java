package io.puharesource.mc.titlemanager.commands.sub;

import org.bukkit.World;
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

import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_BROADCAST_BASIC_SUCCESS_ANIMATION;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_BROADCAST_BASIC_SUCCESS_WITH_SUBTITLE;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_BROADCAST_BASIC_SUCCESS_WITH_TITLE;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_BROADCAST_BUNGEECORD_SUCCESS_ANIMATION;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_BROADCAST_BUNGEECORD_SUCCESS_ANIMATION_TO_SERVER;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_BROADCAST_BUNGEECORD_SUCCESS_WITH_SUBTITLE;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_BROADCAST_BUNGEECORD_SUCCESS_WITH_SUBTITLE_TO_SERVER;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_BROADCAST_BUNGEECORD_SUCCESS_WITH_TITLE;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_BROADCAST_BUNGEECORD_SUCCESS_WITH_TITLE_TO_SERVER;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_BROADCAST_DESCRIPTION;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_BROADCAST_USAGE;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_BROADCAST_WORLD_SUCCESS_ANIMATION;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_BROADCAST_WORLD_SUCCESS_WITH_SUBTITLE;
import static io.puharesource.mc.titlemanager.backend.language.Messages.COMMAND_BROADCAST_WORLD_SUCCESS_WITH_TITLE;
import static io.puharesource.mc.titlemanager.backend.language.Messages.INVALID_RADIUS;
import static io.puharesource.mc.titlemanager.backend.language.Messages.WRONG_WORLD;
import static io.puharesource.mc.titlemanager.commands.CommandParameterIdentifier.BUNGEE;
import static io.puharesource.mc.titlemanager.commands.CommandParameterIdentifier.FADE_IN;
import static io.puharesource.mc.titlemanager.commands.CommandParameterIdentifier.FADE_OUT;
import static io.puharesource.mc.titlemanager.commands.CommandParameterIdentifier.RADIUS;
import static io.puharesource.mc.titlemanager.commands.CommandParameterIdentifier.SILENT;
import static io.puharesource.mc.titlemanager.commands.CommandParameterIdentifier.STAY;
import static io.puharesource.mc.titlemanager.commands.CommandParameterIdentifier.WORLD;

@ParameterSupport(supportedParams = {SILENT, FADE_IN, STAY, FADE_OUT, WORLD, BUNGEE, RADIUS})
public final class SubBroadcast extends TMSubCommand {
    public SubBroadcast() {
        super("bc", "titlemanager.command.broadcast", COMMAND_BROADCAST_USAGE, COMMAND_BROADCAST_DESCRIPTION, "broadcast");
    }

    @Override
    public void onCommand(final CommandSender sender, final String[] args, final CommandParameters params) throws TMCommandException {
        if (args.length < 1) {
            syntaxError(sender);
            return;
        }

        final Optional<BungeeServerInfo> optionalServer = params.getServer(BUNGEE);
        final boolean silent = params.contains(SILENT);
        final ConfigMain config = TitleManager.getInstance().getConfigManager().getConfig();

        final int fadeIn = params.getInt(FADE_IN, config.welcomeMessageFadeIn);
        final int stay = params.getInt(STAY, config.welcomeMessageStay);
        final int fadeOut = params.getInt(FADE_OUT, config.welcomeMessageFadeOut);

        final String[] lines = MiscellaneousUtils.splitString(MiscellaneousUtils.combineArray(0, args));
        final ITitleObject object = MiscellaneousUtils.generateTitleObject(lines[0], lines[1], fadeIn, stay, fadeOut);

        if (params.containsValue(WORLD)) {
            final Optional<World> worldOptional = params.getWorld(WORLD);

            final World world;
            if (!worldOptional.isPresent() && sender instanceof Player) {
                world = worldOptional.orElse(((Player) sender).getWorld());
            } else {
                world = worldOptional.get();
            }

            if(sender instanceof Player) {
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
                sendSuccess(silent, sender, COMMAND_BROADCAST_WORLD_SUCCESS_ANIMATION, world.getName());
            } else {
                final TitleObject title = (TitleObject) object;

                if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                    sendSuccess(silent, sender, COMMAND_BROADCAST_WORLD_SUCCESS_WITH_SUBTITLE, ((TitleObject) object).getTitle(), world.getName());
                } else {
                    sendSuccess(silent, sender, COMMAND_BROADCAST_WORLD_SUCCESS_WITH_TITLE, ((TitleObject) object).getTitle(), world.getName());
                }
            }
        } else if (params.containsValue(BUNGEE)) {
            final BungeeManager manager = TitleManager.getInstance().getBungeeManager();
            final String json = manager.getGson().toJson(object);

            if (optionalServer.isPresent()) {
                final BungeeServerInfo server = optionalServer.get();

                server.sendMessage("TitleObject-Broadcast", json);

                if (object instanceof IAnimation) {
                    sendSuccess(silent, sender, COMMAND_BROADCAST_BUNGEECORD_SUCCESS_ANIMATION_TO_SERVER);
                } else {
                    final TitleObject title = (TitleObject) object;

                    if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                        sendSuccess(silent, sender, COMMAND_BROADCAST_BUNGEECORD_SUCCESS_WITH_SUBTITLE_TO_SERVER, title.getTitle(), title.getSubtitle(), server.getName());
                    } else {
                        sendSuccess(silent, sender, COMMAND_BROADCAST_BUNGEECORD_SUCCESS_WITH_TITLE_TO_SERVER, title.getTitle(), server.getName());
                    }
                }
            } else {
                manager.broadcastBungeeMessage("TitleObject-Broadcast", json);

                if (object instanceof IAnimation) {
                    sendSuccess(silent, sender, COMMAND_BROADCAST_BUNGEECORD_SUCCESS_ANIMATION);
                } else {
                    final TitleObject title = (TitleObject) object;

                    if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                        sendSuccess(silent, sender, COMMAND_BROADCAST_BUNGEECORD_SUCCESS_WITH_SUBTITLE, ((TitleObject) object).getTitle());
                    } else {
                        sendSuccess(silent, sender, COMMAND_BROADCAST_BUNGEECORD_SUCCESS_WITH_TITLE, ((TitleObject) object).getTitle());
                    }
                }
            }
        } else {
            if(sender instanceof Player && params.containsValue(RADIUS)) {
                MiscellaneousUtils
                        .getWithinRadius(((Player) sender).getLocation(), params.get(RADIUS).getDouble(5))
                        .forEach(object::send);
            } else {
                object.broadcast();
            }

            if (object instanceof IAnimation) {
                sendSuccess(silent, sender, COMMAND_BROADCAST_BASIC_SUCCESS_ANIMATION);
            } else {
                final TitleObject title = (TitleObject) object;

                if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                    sendSuccess(silent, sender, COMMAND_BROADCAST_BASIC_SUCCESS_WITH_SUBTITLE, title.getTitle(), title.getSubtitle());
                } else {
                    sendSuccess(silent, sender, COMMAND_BROADCAST_BASIC_SUCCESS_WITH_TITLE, title.getTitle());
                }
            }
        }
    }
}
