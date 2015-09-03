package io.puharesource.mc.titlemanager.commands.sub;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;
import io.puharesource.mc.titlemanager.backend.bungee.BungeeServerInfo;
import io.puharesource.mc.titlemanager.backend.config.ConfigMain;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import io.puharesource.mc.titlemanager.commands.CommandParameter;
import io.puharesource.mc.titlemanager.commands.ParameterSupport;
import io.puharesource.mc.titlemanager.commands.TMSubCommand;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

@ParameterSupport(supportedParams = {"SILENT", "FADEIN", "STAY", "FADEOUT", "WORLD", "BUNGEE", "RADIUS"})
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

        BungeeServerInfo server = null;
        final boolean silent = params.containsKey("SILENT");
        final ConfigMain config = TitleManager.getInstance().getConfigManager().getConfig();

        int fadeIn = config.welcomeMessageFadeIn;
        int stay = config.welcomeMessageStay;
        int fadeOut = config.welcomeMessageFadeOut;

        World world = null;

        if(params.containsKey("BUNGEE")) {
            final CommandParameter param = params.get("BUNGEE");

            if (param.getValue() != null && !param.getValue().isEmpty()) {
                server = TitleManager.getInstance().getBungeeManager().getServers().get(param.getValue().toUpperCase());
            }
        }

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

        if (params.containsKey("WORLD")) {
            CommandParameter param = params.get("WORLD");
            if (param.getValue() != null) {
                world = Bukkit.getWorld(param.getValue());
            }
        }

        final String[] lines = MiscellaneousUtils.splitString(MiscellaneousUtils.combineArray(0, args));
        final ITitleObject object = MiscellaneousUtils.generateTitleObject(lines[0], lines[1], fadeIn, stay, fadeOut);

        if (params.containsKey("WORLD")) {//No support for radius if world != own world
            if (world == null) {
                sendError(sender, "Invalid world!");
            } else {
                boolean b = true;
                if(sender instanceof Player && (((Player)sender).getWorld()==world) {
                    if(params.containsKey("RADIUS") && param.getValue()!=null) {
                        try {
                        for(final Player player : MiscellaneousUtils.getWithinRadius(((Player)sender).getLocation(), Integer.parseInt(param.getValue()))) {
                                object.send(player);
                            }
                            b = false;
                        } catch(Exception e) {
                            //Optional: tell the user that the param value must be a number?
                        }
                    }
                }
                
                if(b) {
                    for (final Player player : world.getPlayers()) {
                        object.send(player);
                    }
                }

                if (silent) return;

                if (object instanceof IAnimation) {
                    sendSuccess(sender, "You have sent a world title animation broadcast to the world \"%s\"", world.getName());
                } else {
                    final TitleObject title = (TitleObject) object;

                    if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                        sendSuccess(sender, "You have sent a world title broadcast with the message \"%s\" \"%s\" to the world \"%s\"", ((TitleObject) object).getTitle(), world.getName());
                    } else {
                        sendSuccess(sender, "You have sent a world title broadcast with the message \"%s\" to the world \"%s\"", ((TitleObject) object).getTitle(), world.getName());
                    }
                }
            }
        } else if (params.containsKey("BUNGEE")) {
            val manager = TitleManager.getInstance().getBungeeManager();
            val json = manager.getGson().toJson(object);

            if (server == null) {
                if (params.get("BUNGEE").getValue() == null) {
                    manager.broadcastBungeeMessage("TitleObject-Broadcast", json);

                    if (silent) return;

                    if (object instanceof IAnimation) {
                        sendSuccess(sender, "You have sent a bungeecord title animation broadcast");
                    } else {
                        final TitleObject title = (TitleObject) object;

                        if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                            sendSuccess(sender, "You have sent a bungeecord title broadcast with the message \"%s\" \"%s\"", ((TitleObject) object).getTitle());
                        } else {
                            sendSuccess(sender, "You have sent a bungeecord title broadcast with the message \"%s\"", ((TitleObject) object).getTitle());
                        }
                    }
                } else {
                    sendError(sender, "%s is an invalid server!", params.get("BUNGEE").getValue());
                }
            } else {
                server.sendMessage("TitleObject-Broadcast", json);

                if (silent) return;

                if (object instanceof IAnimation) {
                    sendSuccess(sender, "You have sent a bungeecord title animation broadcast");
                } else {
                    final TitleObject title = (TitleObject) object;

                    if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                        sendSuccess(sender, "You have sent a bungeecord title broadcast with the message \"%s\" \"%s\" to the server \"%s\"", title.getTitle(), title.getSubtitle(), server.getName());
                    } else {
                        sendSuccess(sender, "You have sent a bungeecord title broadcast with the message \"%s\" to the server \"%s\"", title.getTitle(), server.getName());
                    }
                }
            }
        } else {
            boolean b = true;
            if(sender instanceof Player) {
                if(params.containsKey("RADIUS") && param.getValue()!=null) {
                    try {
                        for(final Player player : MiscellaneousUtils.getWithinRadius(((Player)sender).getLocation(), Integer.parseInt(param.getValue()))) {
                            object.send(player);
                        }
                        b = false;
                    } catch(Exception e) {
                        //Optional: tell the user that the param value must be a number?
                    }
                }
            }
            if(b) {
                object.broadcast();
            }

            if (silent) return;

            if (object instanceof IAnimation) {
                sendSuccess(sender, "You have sent an title animation broadcast.");
            } else {
                final TitleObject title = (TitleObject) object;

                if (title.getSubtitle() != null && !title.getSubtitle().isEmpty()) {
                    sendSuccess(sender, "You have sent a title broadcast with the message \"%s\" \"%s\"", title.getTitle(), title.getSubtitle());
                } else {
                    sendSuccess(sender, "You have sent a title broadcast with the message \"%s\"", title.getTitle());
                }
            }
        }
    }
}
