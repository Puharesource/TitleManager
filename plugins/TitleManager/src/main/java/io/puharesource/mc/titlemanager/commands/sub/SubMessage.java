package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;
import io.puharesource.mc.titlemanager.backend.config.ConfigMain;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import io.puharesource.mc.titlemanager.commands.CommandParameter;
import io.puharesource.mc.titlemanager.commands.ParameterSupport;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

@ParameterSupport(supportedParams = {"BUNGEE", "SILENT", "FADEIN", "STAY", "FADEOUT"})
public final class SubMessage extends TMSubCommand {
    public SubMessage() {
        super("msg", "titlemanager.command.message", "<player> <message>", "Sends a title message to the specified player, put inside of the message, to add a subtitle.", "message");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args, Map<String, CommandParameter> params) {
        if (args.length < 2) {
            syntaxError(sender);
            return;
        }
        
        if(params.containsKey("BUNGEE")) {//cannot confirm player online/offline easily
            String cmd = "tm msg ";
            for(String s : params.keySet()) {
                cmd+="-"+s;
                if(params.get(s).getValue()!=null) {
                    cmd+="="+params.get(s).getValue();
                }
                cmd+=" ";
            }
            for(String s : args) {
                cmd += args+" ";
            }
            BungeeManager m = TitleManager.getInstance().getBungeeManager();
            m.sendMessage("Broadcast", cmd);
            boolean silent = params.containsKey("SILENT");
            if(!silent) {
                if (object instanceof IAnimation) {
                    sender.sendMessage(ChatColor.GREEN + "You have sent a bungeecord message animation.");
                } else {
                    TitleObject titleObject = (TitleObject) object;
                    if (titleObject.getSubtitle() != null && !titleObject.getSubtitle().isEmpty())
                        sender.sendMessage(ChatColor.GREEN + "You have sent a bungeecord message with the message \"" + ChatColor.RESET + titleObject.getTitle() + ChatColor.GREEN + "\" \"" + ChatColor.RESET + titleObject.getSubtitle() + ChatColor.GREEN + "\"");
                    else sender.sendMessage(ChatColor.GREEN + "You have sent a bungeecord message with the message \"" + ChatColor.RESET + titleObject.getTitle() + ChatColor.GREEN + "\"");
                }
            }
            return;
        }

        Player player = MiscellaneousUtils.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + args[0] + " is not a player!");
            return;
        }

        boolean silent = params.containsKey("SILENT");
        ConfigMain config = TitleManager.getInstance().getConfigManager().getConfig();
        int fadeIn = config.welcomeMessageFadeIn;
        int stay = config.welcomeMessageStay;
        int fadeOut = config.welcomeMessageFadeOut;

        if (params.containsKey("FADEIN")) {
            CommandParameter param = params.get("FADEIN");
            if (param.getValue() != null) {
                try {
                    fadeIn = Integer.valueOf(param.getValue());
                } catch (NumberFormatException ignored) {}
            }
        }
        if (params.containsKey("STAY")) {
            CommandParameter param = params.get("STAY");
            if (param.getValue() != null) {
                try {
                    stay = Integer.valueOf(param.getValue());
                } catch (NumberFormatException ignored) {}
            }
        }
        if (params.containsKey("FADEOUT")) {
            CommandParameter param = params.get("FADEOUT");
            if (param.getValue() != null) {
                try {
                    fadeOut = Integer.valueOf(param.getValue());
                } catch (NumberFormatException ignored) {}
            }
        }

        String text = MiscellaneousUtils.combineArray(1, args);
        text = text.replaceFirst("[%{][Nn][Ll][%}]","<nl>");

        String[] lines = text.toLowerCase().contains("<nl>") ? text.split("(?i)<nl>") : new String[]{text, ""};

        ITitleObject object = MiscellaneousUtils.generateTitleObject(lines[0], lines[1] == null ? "" : lines[1], fadeIn, stay, fadeOut);

        if (!silent) {
            if (object instanceof IAnimation) {
                sender.sendMessage(ChatColor.GREEN + "You have sent an animation to " + player.getName() + ".");
            } else {
                TitleObject titleObject = (TitleObject) object;

                if (titleObject.getSubtitle() != null)
                    sender.sendMessage(ChatColor.GREEN + "You have sent " + ChatColor.stripColor(player.getDisplayName()) + " \"" + ChatColor.RESET + titleObject.getTitle() + ChatColor.GREEN + "\" \"" + ChatColor.RESET + titleObject.getSubtitle() + ChatColor.GREEN + "\"");
                else sender.sendMessage(ChatColor.GREEN + "You have sent " + ChatColor.stripColor(player.getDisplayName()) + " \"" + ChatColor.RESET + titleObject.getTitle() + ChatColor.GREEN + "\"");
            }
        }

        object.send(player);
    }
}
