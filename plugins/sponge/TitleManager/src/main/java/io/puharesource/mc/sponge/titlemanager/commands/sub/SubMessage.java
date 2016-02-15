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

import java.util.Optional;

import static io.puharesource.mc.sponge.titlemanager.MiscellaneousUtils.*;

public final class SubMessage extends TMSubCommand {
    @Inject private TitleManager plugin;

    public SubMessage() {
        super("SILENT", "FADEIN", "STAY", "FADEOUT");
    }

    @Override
    public void onCommand(final CommandSource source, final CommandContext args, final CommandParameters params) throws TMCommandException {
        if (!(args.hasAny("player") || args.hasAny("message"))) {
            syntaxError(source);
            return;
        }

        final Optional<Player> oPlayer = args.<Player>getOne("player");
        final Optional<String> oMessage = args.<String>getOne("message");

        if (!oPlayer.isPresent()) {
            sendError(source, plugin.getConfigHandler().getMessage("general.invalid_player"));
            return;
        } else if (!oMessage.isPresent()) {
            syntaxError(source);
            return;
        }

        final boolean silent = params.getBoolean("SILENT");
        final ConfigMain config = plugin.getConfigHandler().getMainConfig().getConfig();
        final int fadeIn = params.getInt("FADEIN", config.welcomeMessageFadeIn);
        final int stay = params.getInt("STAY", config.welcomeMessageStay);
        final int fadeOut = params.getInt("FADEOUT", config.welcomeMessageFadeOut);

        final String[] lines = splitString(oMessage.get());
        final TitleSendable titleObject = generateTitleObject(format(lines[0]), format(lines[1]), fadeIn, stay, fadeOut);
        final Player player = oPlayer.get();

        titleObject.send(player);

        if (silent) return;

        if (titleObject instanceof AnimationSendable) {
            sendSuccess(source, plugin.getConfigHandler().getMessage("command.message.success_animation", player.getName()));
        } else {
            final TitleObject title = (TitleObject) titleObject;

            if (title.getSubtitle().isPresent() && !title.getSubtitle().get().isEmpty()) {
                sendSuccess(source, plugin.getConfigHandler().getMessage("command.message.success_with_subtitle", title.getTitle().get().toPlain(), title.getSubtitle().get().toPlain(), player.getName()));
            } else {
                sendSuccess(source, plugin.getConfigHandler().getMessage("command.message.success_without_subtitle", title.getTitle().get().toPlain(), player.getName()));
            }
        }
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .permission("titlemanager.command.message")
                .description(Text.of(plugin.getConfigHandler().getMessage("command.message.description")))
                .extendedDescription(Text.of(plugin.getConfigHandler().getMessage("command.message.description_extended")))
                .arguments(GenericArguments.player(Text.of("player")), GenericArguments.remainingJoinedStrings(Text.of("message")))
                .inputTokenizer(createTokenizer())
                .executor(this)
                .build();
    }
}
