package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import io.puharesource.mc.titlemanager.commands.CommandParameter;
import io.puharesource.mc.titlemanager.commands.ParameterSupport;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

@ParameterSupport(supportedParams = {"SILENT"})
public final class SubAMessage extends TMSubCommand {
    public SubAMessage() {
        super("amsg", "titlemanager.command.amessage", "<player> <message>", "Sends an actionbar title message to the specified player.", "amessage");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args, Map<String, CommandParameter> params) {
        if (args.length < 2) {
            syntaxError(sender);
            return;
        }

        boolean silent = params.containsKey("SILENT");

        Player player = MiscellaneousUtils.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + args[0] + " is not currently online!");
            return;
        }

        String text = MiscellaneousUtils.combineArray(1, args);

        IActionbarObject object = MiscellaneousUtils.generateActionbarObject(text);

        if (!silent) {
            if (object instanceof IAnimation)
                sender.sendMessage(ChatColor.GREEN + "You have sent an actionbar animation to " + player.getName() + ".");
            else sender.sendMessage(ChatColor.GREEN + "You have sent " + ChatColor.stripColor(player.getDisplayName()) + " \"" + ChatColor.RESET + ((ActionbarTitleObject) object).getTitle() + ChatColor.GREEN + "\"");
        }

        object.send(player);
    }
}
