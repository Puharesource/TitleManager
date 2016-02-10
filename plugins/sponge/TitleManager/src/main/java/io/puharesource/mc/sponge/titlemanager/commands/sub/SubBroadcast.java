package io.puharesource.mc.sponge.titlemanager.commands.sub;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.MiscellaneousUtils;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.api.TitleObject;
import io.puharesource.mc.sponge.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.sponge.titlemanager.api.iface.ITitleObject;
import io.puharesource.mc.sponge.titlemanager.commands.CommandParameters;
import io.puharesource.mc.sponge.titlemanager.commands.TMCommandException;
import io.puharesource.mc.sponge.titlemanager.commands.TMSubCommand;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import java.util.Optional;

import static io.puharesource.mc.sponge.titlemanager.Messages.*;

public final class SubBroadcast extends TMSubCommand {
    @Inject private TitleManager plugin;

    public SubBroadcast() {
        super("SILENT", "FADEIN", "STAY", "FADEOUT", "WORLD", "RADIUS");
    }

    @Override
    public void onCommand(final CommandSource source, final CommandContext args, final CommandParameters params) throws TMCommandException {
        if (args.hasAny("message")) {
            syntaxError(source);
            return;
        }

        final String message = args.<String>getOne("message").get();
        final boolean silent = params.getBoolean("SILENT");
        final ConfigMain config = plugin.getConfigHandler().getConfig();

        final int fadeIn = params.getInt("FADEIN", config.welcomeMessageFadeIn);
        final int stay = params.getInt("STAY", config.welcomeMessageStay);
        final int fadeOut = params.getInt("FADEOUT", config.welcomeMessageFadeOut);
        final Optional<World> oWorld = params.getWorld("WORLD");
        final Optional<Double> oRadius = params.getDouble("radius");

        final String[] lines = MiscellaneousUtils.splitString(message);
        final ITitleObject object = MiscellaneousUtils.generateTitleObject(lines[0], lines[1], fadeIn, stay, fadeOut);

        if (params.contains("WORLD")) {//No support for radius if world != own world
            if (!oWorld.isPresent()) throw new TMCommandException(INVALID_WORLD, params.get("WORLD").getValue());
            final World world = oWorld.get();

            if(source instanceof Player && (((Player) source).getWorld().equals(world)) && params.contains("RADIUS") && oRadius.isPresent()) {
                MiscellaneousUtils.getWithinRadius(((Player) source).getLocation(), oRadius.get()).forEach(object::send);
            } else if (params.contains("RADIUS") && !oRadius.isPresent()) {
                throw new TMCommandException(WRONG_WORLD);
            } else {
                object.broadcast(world);
            }

            if (silent) return;

            if (object instanceof IAnimation) {
                sendSuccess(source, COMMAND_BROADCAST_WORLD_SUCCESS_ANIMATION, world.getName());
            } else {
                final TitleObject title = (TitleObject) object;

                if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                    sendSuccess(source, COMMAND_BROADCAST_WORLD_SUCCESS_WITH_SUBTITLE, ((TitleObject) object).getTitle(), world.getName());
                } else {
                    sendSuccess(source, COMMAND_BROADCAST_WORLD_SUCCESS_WITH_TITLE, ((TitleObject) object).getTitle(), world.getName());
                }
            }
        } else {
            if(source instanceof Player && oRadius.isPresent()) {
                MiscellaneousUtils.getWithinRadius(((Player)source).getLocation(), oRadius.get()).forEach(object::send);
            } else {
                object.broadcast();
            }

            if (silent) return;

            if (object instanceof IAnimation) {
                sendSuccess(source, COMMAND_BROADCAST_BASIC_SUCCESS_ANIMATION);
            } else {
                final TitleObject title = (TitleObject) object;

                if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                    sendSuccess(source, COMMAND_BROADCAST_BASIC_SUCCESS_WITH_SUBTITLE, title.getTitle(), title.getSubtitle());
                } else {
                    sendSuccess(source, COMMAND_BROADCAST_BASIC_SUCCESS_WITH_TITLE, title.getTitle());
                }
            }
        }
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .permission("titlemanager.command.broadcast")
                .description(Text.of("Sends a title broadcast."))
                .extendedDescription(Text.of("Sends a title message to everyone on the server, put <nl> or {nl} or %nl% inside the message, to add a subtitle."))
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("message")))
                .inputTokenizer(createTokenizer())
                .executor(this)
                .build();
    }
}
