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

import java.util.Optional;

import static io.puharesource.mc.sponge.titlemanager.Messages.*;

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
            sendError(source, INVALID_PLAYER);
            return;
        } else if (!oMessage.isPresent()) {
            syntaxError(source);
            return;
        }

        final boolean silent = params.getBoolean("SILENT");
        final ConfigMain config = plugin.getConfigHandler().getConfig();
        final int fadeIn = params.getInt("FADEIN", config.welcomeMessageFadeIn);
        final int stay = params.getInt("STAY", config.welcomeMessageStay);
        final int fadeOut = params.getInt("FADEOUT", config.welcomeMessageFadeOut);

        final String[] lines = MiscellaneousUtils.splitString(oMessage.get());
        final ITitleObject titleObject = MiscellaneousUtils.generateTitleObject(lines[0], lines[1], fadeIn, stay, fadeOut);
        final Player player = oPlayer.get();

        titleObject.send(player);

        if (silent) return;

        if (titleObject instanceof IAnimation) {
            sendSuccess(source, COMMAND_MESSAGE_BASIC_SUCCESS_ANIMATION, player.getName());
        } else {
            final TitleObject title = (TitleObject) titleObject;

            if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                sendSuccess(source, COMMAND_MESSAGE_BASIC_SUCCESS_WITH_SUBTITLE, title.getTitle(), title.getSubtitle(), player.getName());
            } else {
                sendSuccess(source, COMMAND_MESSAGE_BASIC_SUCCESS_WITH_TITLE, title.getTitle(), player.getName());
            }
        }
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .permission("titlemanager.command.message")
                .description(Text.of("Messages the player a title."))
                .extendedDescription(Text.of("Sends a title message to the specified player, put <nl> or {nl} or %nl% inside of the message, to add a subtitle."))
                .arguments(GenericArguments.player(Text.of("player")), GenericArguments.remainingJoinedStrings(Text.of("message")))
                .inputTokenizer(createTokenizer())
                .executor(this)
                .build();
    }
}
