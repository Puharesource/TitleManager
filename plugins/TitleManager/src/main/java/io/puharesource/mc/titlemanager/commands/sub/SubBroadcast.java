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

import java.util.Map;

+@ParameterSupport(supportedParams = {"SILENT", "FADEIN", "STAY", "FADEOUT", "WORLD"})
public final class SubBroadcast extends TMSubCommand {
    public SubBroadcast() {
        super("bc", "titlemanager.command.broadcast", "<message>", "Sends a title message to everyone on the server, put inside of the message, to add a subtitle.", "broadcast");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args, Map<String, CommandParameter> params) {
        if (args.length < 1) {
            syntaxError(sender);
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
+       if(params.containsKey("WORLD")) {
+           CommandParameter param = params.get("WORLD");
+           if(param.getValue()!=null) {
+               world = Bukkit.getWorld(param.getValue());
+           }
+       }

        String text = MiscellaneousUtils.combineArray(0, args);
        text = text.replaceFirst("[%{][Nn][Ll][%}]","<nl>");

        String[] lines = text.toLowerCase().contains("<nl>") ? text.split("(?i)<nl>") : new String[]{text, ""};

        ITitleObject object = MiscellaneousUtils.generateTitleObject(lines[0], lines[1] == null ? "" : lines[1], fadeIn, stay, fadeOut);

        if (!silent) {
            if (object instanceof IAnimation) {
                sender.sendMessage(ChatColor.GREEN + "You have sent a broadcast animation.");
            } else {
                TitleObject titleObject = (TitleObject) object;

                if (titleObject.getSubtitle() != null && !titleObject.getSubtitle().isEmpty())
                    sender.sendMessage(ChatColor.GREEN + "You have sent a broadcast with the message \"" + ChatColor.RESET + titleObject.getTitle() + ChatColor.GREEN + "\" \"" + ChatColor.RESET + titleObject.getSubtitle() + ChatColor.GREEN + "\"");
                else sender.sendMessage(ChatColor.GREEN + "You have sent a broadcast with the message \"" + ChatColor.RESET + titleObject.getTitle() + ChatColor.GREEN + "\"");
            }
        }

+        if(world!=null) {
+            for(Player p : Bukkit.getOnlinePlayers()) {
+                if(p.getWorld()==world) {
+                    object.send(p);
+                }
+            }
+            return;
+        }
        object.broadcast();
    }
}
