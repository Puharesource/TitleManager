package io.puharesource.mc.sponge.titlemanager.commands.sub;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.api.TitleObject;
import io.puharesource.mc.sponge.titlemanager.api.iface.AnimationSendable;
import io.puharesource.mc.sponge.titlemanager.api.iface.TitleSendable;
import io.puharesource.mc.sponge.titlemanager.commands.CommandParameters;
import io.puharesource.mc.sponge.titlemanager.commands.TMCommandException;
import io.puharesource.mc.sponge.titlemanager.commands.TMSubCommand;
import io.puharesource.mc.sponge.titlemanager.config.configs.ConfigMain;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import java.util.Optional;

import static io.puharesource.mc.sponge.titlemanager.MiscellaneousUtils.*;

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
        final ConfigMain config = plugin.getConfigHandler().getMainConfig().getConfig();

        final int fadeIn = params.getInt("FADEIN", config.welcomeMessageFadeIn);
        final int stay = params.getInt("STAY", config.welcomeMessageStay);
        final int fadeOut = params.getInt("FADEOUT", config.welcomeMessageFadeOut);
        final Optional<World> oWorld = params.getWorld("WORLD");
        final Optional<Double> oRadius = params.getDouble("radius");

        final String[] lines = splitString(message);
        final TitleSendable object = generateTitleObject(format(lines[0]), format(lines[1]), fadeIn, stay, fadeOut);

        if (params.contains("WORLD")) {
            if (!oWorld.isPresent()) throw new TMCommandException(plugin.getConfigHandler().getMessage("general.invalid_world"));
            final World world = oWorld.get();

            if(source instanceof Player && (((Player) source).getWorld().equals(world)) && params.contains("RADIUS") && oRadius.isPresent()) {
                getWithinRadius(((Player) source).getLocation(), oRadius.get()).forEach(object::send);
            } else if (params.contains("RADIUS") && !oRadius.isPresent()) {
                throw new TMCommandException(plugin.getConfigHandler().getMessage("general.wrong_world"));
            } else {
                object.broadcast(world);
            }

            if (silent) return;

            if (object instanceof AnimationSendable) {
                sendSuccess(source, plugin.getConfigHandler().getMessage("command.broadcast.success_world_animations", world.getName()));
            } else {
                final TitleObject title = (TitleObject) object;

                if (title.getSubtitle().isPresent() && !title.getSubtitle().get().isEmpty()) {
                    sendSuccess(source, plugin.getConfigHandler().getMessage("command.broadcast.success_world_with_subtitle", ((TitleObject) object).getTitle().get().toPlain(), ((TitleObject) object).getSubtitle().get().toPlain(), world.getName()));
                } else {
                    sendSuccess(source, plugin.getConfigHandler().getMessage("command.broadcast.success_world_without_subtitle", ((TitleObject) object).getTitle().get().toPlain(), world.getName()));
                }
            }
        } else {
            if(source instanceof Player && oRadius.isPresent()) {
                getWithinRadius(((Player)source).getLocation(), oRadius.get()).forEach(object::send);
            } else {
                object.broadcast();
            }

            if (silent) return;

            if (object instanceof AnimationSendable) {
                sendSuccess(source, plugin.getConfigHandler().getMessage("command.broadcast.success_animation"));
            } else {
                final TitleObject title = (TitleObject) object;

                if (title.getSubtitle().isPresent() && !title.getSubtitle().get().isEmpty()) {
                    sendSuccess(source, plugin.getConfigHandler().getMessage("command.broadcast.success_world_with_subtitle", title.getTitle().get().toPlain(), title.getSubtitle().get().toPlain()));
                } else {
                    sendSuccess(source, plugin.getConfigHandler().getMessage("command.broadcast.success_world_without_subtitle", title.getTitle().get().toPlain()));
                }
            }
        }
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .permission("titlemanager.command.broadcast")
                .description(Text.of(plugin.getConfigHandler().getMessage("command.broadcast.description")))
                .extendedDescription(Text.of(plugin.getConfigHandler().getMessage("command.broadcast.description_extended")))
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("message")))
                .inputTokenizer(createTokenizer())
                .executor(this)
                .build();
    }
}
