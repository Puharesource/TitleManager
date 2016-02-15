package io.puharesource.mc.sponge.titlemanager.commands.sub;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.ConfigHandler;
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

public final class SubABroadcast extends TMSubCommand {
    @Inject private TitleManager plugin;

    public SubABroadcast() {
        super("SILENT", "WORLD", "RADIUS");
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .permission("titlemanager.command.abroadcast")
                .description(Text.of(plugin.getConfigHandler().getMessage("command.abroadcast.description")))
                .extendedDescription(Text.of(plugin.getConfigHandler().getMessage("command.abroadcast.extended_description")))
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
        final ConfigHandler configHandler = plugin.getConfigHandler();

        final Optional<World> oWorld = params.getWorld("WORLD");
        final Optional<Double> oRadius = params.getDouble("RADIUS");
        final ActionbarSendable sendable = generateActionbarObject(format(message));

        if (params.contains("WORLD")) {
            if (!oWorld.isPresent()) throw new TMCommandException(configHandler.getMessage("general.invalid_world"));
            final World world = oWorld.get();

            if(source instanceof Player && (((Player) source).getWorld().equals(world)) && params.contains("RADIUS") && oRadius.isPresent()) {
                getWithinRadius(((Player) source).getLocation(), oRadius.get()).forEach(sendable::send);
            } else if (params.contains("RADIUS") && !oRadius.isPresent()) {
                throw new TMCommandException(configHandler.getMessage("general.wrong_world"));
            } else {
                sendable.broadcast(world);
            }

            if (silent) return;

            if (sendable instanceof AnimationSendable) {
                sendSuccess(source, configHandler.getMessage("command.abroadcast.success_world_animation", world.getName()));
            } else {
                sendSuccess(source, configHandler.getMessage("command.abroadcast.success_world", ((ActionbarTitleObject) sendable).getTitle().toPlain(), world.getName()));
            }
        } else {
            if(source instanceof Player && oRadius.isPresent()) {
                getWithinRadius(((Player)source).getLocation(), oRadius.get()).forEach(sendable::send);
            } else {
                sendable.broadcast();
            }

            if (silent) return;

            if (sendable instanceof AnimationSendable) {
                sendSuccess(source, configHandler.getMessage("command.abroadcast.success_animation"));
            } else {
                sendSuccess(source, configHandler.getMessage("command.abroadcast.success", ((ActionbarTitleObject) sendable).getTitle().toPlain()));
            }
        }
    }
}
