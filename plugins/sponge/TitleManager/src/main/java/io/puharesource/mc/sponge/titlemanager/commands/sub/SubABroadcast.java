package io.puharesource.mc.sponge.titlemanager.commands.sub;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.api.ActionbarTitleObject;
import io.puharesource.mc.sponge.titlemanager.api.iface.ActionbarSendable;
import io.puharesource.mc.sponge.titlemanager.api.iface.AnimationSendable;
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

import static io.puharesource.mc.sponge.titlemanager.MiscellaneousUtils.*;
import static io.puharesource.mc.sponge.titlemanager.Messages.*;

public final class SubABroadcast extends TMSubCommand {
    @Inject private TitleManager plugin;

    public SubABroadcast() {
        super("SILENT", "WORLD", "RADIUS");
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .permission("titlemanager.command.abroadcast")
                .description(Text.of("Sends an actionbar broadcast."))
                .extendedDescription(Text.of("Sends an actionbar title message to everyone on the server."))
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("message")))
                .inputTokenizer(createTokenizer())
                .executor(this)
                .build();
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
        final ActionbarSendable sendable = generateActionbarObject(format(message));

        if (params.contains("WORLD")) {
            if (!oWorld.isPresent()) throw new TMCommandException(INVALID_WORLD, params.get("WORLD").getValue());
            final World world = oWorld.get();

            if(source instanceof Player && (((Player) source).getWorld().equals(world)) && params.contains("RADIUS") && oRadius.isPresent()) {
                getWithinRadius(((Player) source).getLocation(), oRadius.get()).forEach(sendable::send);
            } else if (params.contains("RADIUS") && !oRadius.isPresent()) {
                throw new TMCommandException(WRONG_WORLD);
            } else {
                sendable.broadcast(world);
            }

            if (silent) return;

            if (sendable instanceof AnimationSendable) {
                sendSuccess(source, COMMAND_ABROADCAST_WORLD_SUCCESS_ANIMATION, world.getName());
            } else {
                sendSuccess(source, COMMAND_ABROADCAST_WORLD_SUCCESS, ((ActionbarTitleObject) sendable).getTitle(), world.getName());
            }
        } else {
            if(source instanceof Player && oRadius.isPresent()) {
                getWithinRadius(((Player)source).getLocation(), oRadius.get()).forEach(sendable::send);
            } else {
                sendable.broadcast();
            }

            if (silent) return;

            if (sendable instanceof AnimationSendable) {
                sendSuccess(source, COMMAND_ABROADCAST_BASIC_SUCCESS_ANIMATION);
            } else {
                sendSuccess(source, COMMAND_ABROADCAST_BASIC_SUCCESS, ((ActionbarTitleObject) sendable).getTitle());
            }
        }
    }
}
