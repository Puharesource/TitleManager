package io.puharesource.mc.sponge.titlemanager.commands.sub;

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

import java.util.Optional;

import static io.puharesource.mc.sponge.titlemanager.MiscellaneousUtils.*;
import static io.puharesource.mc.sponge.titlemanager.Messages.*;

public final class SubAMessage extends TMSubCommand {
    public SubAMessage() {
        super("SILENT");
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .permission("titlemanager.command.amessage")
                .description(Text.of("Messages the player an actionbar title."))
                .extendedDescription(Text.of("Sends an actionbar title message to the specified player."))
                .arguments(GenericArguments.player(Text.of("player")), GenericArguments.remainingJoinedStrings(Text.of("message")))
                .inputTokenizer(createTokenizer())
                .executor(this)
                .build();
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

        final Player player = oPlayer.get();
        final String message = oMessage.get();

        final boolean silent = params.getBoolean("SILENT");
        final ActionbarSendable actionbarObject = generateActionbarObject(format(message));

        actionbarObject.send(player);

        if (silent) return;

        if (actionbarObject instanceof AnimationSendable)
            sendSuccess(source, COMMAND_AMESSAGE_BASIC_SUCCESS, player.getName());
        else sendSuccess(source, COMMAND_AMESSAGE_BASIC_SUCCESS_ANIMATION, player.getName(), ((ActionbarTitleObject) actionbarObject).getTitle());
    }
}
