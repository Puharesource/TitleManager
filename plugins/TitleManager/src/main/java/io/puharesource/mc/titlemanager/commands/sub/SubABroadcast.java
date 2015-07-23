package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import io.puharesource.mc.titlemanager.commands.CommandParameter;
import io.puharesource.mc.titlemanager.commands.ParameterSupport;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

@ParameterSupport(supportedParams = {"SILENT", "WORLD"})
public final class SubABroadcast extends TMSubCommand {
    public SubABroadcast() {
        super("abc", "titlemanager.command.abroadcast", "<message>", "Sends an actionbar title message to everyone on the server.", "abroadcast");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args, Map<String, CommandParameter> params) {
        if (args.length < 1) {
            syntaxError(sender);
            return;
        }

        World world = null;
        boolean silent = params.containsKey("SILENT");

        String text = MiscellaneousUtils.combineArray(0, args);

        IActionbarObject object = MiscellaneousUtils.generateActionbarObject(text);

        if(params.containsKey("WORLD")) {
            CommandParameter param = params.get("WORLD");
            if(param.getValue() != null) {
                world = Bukkit.getWorld(param.getValue());
            }
        }

        if (!silent) {
            if (object instanceof IAnimation)
                sender.sendMessage(ChatColor.GREEN + "You have sent an actionbar animation broadcast.");
            else sender.sendMessage(ChatColor.GREEN + "You have sent an actionbar broadcast with the message \"" + ChatColor.RESET + ((ActionbarTitleObject) object).getTitle() + ChatColor.GREEN + "\"");
        }

        if (world != null) {
            for (final Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld() == world) {
                    object.send(player);
                }
            }
        } else {
            object.broadcast();
        }
    }
}
