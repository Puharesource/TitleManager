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

import java.util.Optional;

import static io.puharesource.mc.sponge.titlemanager.utils.MiscellaneousUtils.format;
import static io.puharesource.mc.sponge.titlemanager.utils.MiscellaneousUtils.createActionbarSendable;

public final class SubAMessage extends TMSubCommand {
    @Inject private TitleManager plugin;

    public SubAMessage() {
        super("SILENT");
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder()
                .permission("titlemanager.command.amessage")
                .description(Text.of(plugin.getConfigHandler().getMessage("command.amessage.description")))
                .extendedDescription(Text.of(plugin.getConfigHandler().getMessage("command.amessage.description_extended")))
                .arguments(GenericArguments.player(Text.of("player")), GenericArguments.remainingJoinedStrings(Text.of("message")))
                //TODO:.inputTokenizer(createTokenizer())
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

        final ConfigHandler configHandler = plugin.getConfigHandler();

        if (!oPlayer.isPresent()) {
            throw new TMCommandException(configHandler.getMessage("general.invalid_player"));
        } else if (!oMessage.isPresent()) {
            throw new TMCommandException(configHandler.getMessage("general.invalid_message"));
        }

        final Player player = oPlayer.get();
        final String message = oMessage.get();

        final boolean silent = params.getBoolean("SILENT");
        final ActionbarSendable actionbarObject = createActionbarSendable(format(message));

        actionbarObject.send(player);

        if (silent) return;

        if (actionbarObject instanceof AnimationSendable)
            sendSuccess(source, configHandler.getMessage("command.amessage.success", player.getName()));
        else sendSuccess(source, configHandler.getMessage("command.amessage.success_animation", player.getName(), ((ActionbarTitleObject) actionbarObject).getTitle().toPlain()));
    }
}
