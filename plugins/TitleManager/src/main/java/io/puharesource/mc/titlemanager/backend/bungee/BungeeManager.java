package io.puharesource.mc.titlemanager.backend.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import io.puharesource.mc.titlemanager.api.TabTitleObject;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.api.animations.*;
import io.puharesource.mc.titlemanager.api.gson.adapters.*;
import io.puharesource.mc.titlemanager.api.gson.adapters.animations.*;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;
import io.puharesource.mc.titlemanager.api.iface.ITabObject;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BungeeManager implements PluginMessageListener {
    private final Map<String, BungeeServerInfo> servers = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(TitleObject.class, new TitleObjectAdapter())
            .registerTypeAdapter(ActionbarTitleObject.class, new ActionbarTitleAdapter())
            .registerTypeAdapter(TabTitleObject.class, new TabTitleObjectAdapter())

            .registerTypeAdapter(AnimationFrame.class, new AnimationFrameAdapter())
            .registerTypeAdapter(FrameSequence.class, new FrameSequenceAdapter())
            .registerTypeAdapter(TitleAnimation.class, new TitleAnimationAdapter())
            .registerTypeAdapter(ActionbarTitleAnimation.class, new ActionbarTitleAnimationAdapter())
            .registerTypeAdapter(TabTitleAnimation.class, new TabTitleAnimationAdapter())

            .registerTypeAdapter(ITitleObject.class, new ITitleObjectDeserializer())
            .registerTypeAdapter(IActionbarObject.class, new IActionbarObjectDeserializer())
            .registerTypeAdapter(ITabObject.class, new ITabTitleObjectDeserializer())

            .create();

    private String currentServer;
    private int onlinePlayers = -1;

    public BungeeManager() {
        Bukkit.getScheduler().runTaskTimer(TitleManager.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (TitleManager.getInstance().getConfigManager().getConfig().usingBungeecord) {
                    sendBungeeMessage("GetServers");
                    sendBungeeMessage("GetServer");
                }
            }
        }, 0l, 200l);
    }

    @Override
    public void onPluginMessageReceived(final String channel, final Player player, final byte[] message) {
        if (!TitleManager.getInstance().getConfigManager().getConfig().usingBungeecord) return;
        if (!channel.equals("BungeeCord")) return;

        val in = ByteStreams.newDataInput(message);
        final String subChannel;

        try {
            subChannel = in.readUTF();
        } catch (Exception e) {
            return; // Doesn't print anything, due to the fact that some plugins might send malformed messages.
        }

        switch (subChannel) {
            case "TitleManager": {
                byte[] bytes = new byte[in.readShort()];
                in.readFully(bytes);
                final ByteArrayDataInput tmIn = ByteStreams.newDataInput(bytes);

                switch (tmIn.readUTF()) {
                    case "TitleObject-Message": {
                        final ITitleObject titleObject = gson.fromJson(tmIn.readUTF(), ITitleObject.class);
                        final String playerName = gson.fromJson(tmIn.readUTF(), String.class);
                        final Player receiver = MiscellaneousUtils.getPlayer(playerName);

                        if (receiver != null) {
                            titleObject.send(receiver);
                        }

                        break;
                    }
                    case "TitleObject-Broadcast": {
                        final ITitleObject titleObject = gson.fromJson(tmIn.readUTF(), ITitleObject.class);

                        titleObject.broadcast();

                        break;
                    }
                    case "ActionbarTitle-Message": {
                        final IActionbarObject titleObject = gson.fromJson(tmIn.readUTF(), IActionbarObject.class);
                        final String playerName = gson.fromJson(tmIn.readUTF(), String.class);
                        final Player receiver = MiscellaneousUtils.getPlayer(playerName);

                        if (receiver != null) {
                            titleObject.send(receiver);
                        }

                        break;
                    }
                    case "ActionbarTitle-Broadcast": {
                        final IActionbarObject titleObject = gson.fromJson(tmIn.readUTF(), IActionbarObject.class);

                        titleObject.broadcast();

                        break;
                    }
                }
                break;
            }
            case "GetServers": {
                final Map<String, String> newServers = new HashMap<>();
                for (String newServer : in.readUTF().split(", ")) {
                    newServers.put(newServer.toUpperCase().trim(), newServer);
                }

                for (final String server : servers.keySet()) {
                    if (!newServers.containsKey(server.toUpperCase().trim())) {
                        servers.remove(server.toUpperCase().trim());
                    }
                }

                for (final Map.Entry<String, String> server : newServers.entrySet()) {
                    if (!servers.containsKey(server.getKey())) {
                        servers.put(server.getKey(), new BungeeServerInfo(server.getValue()));
                    }

                    servers.get(server.getKey()).update();
                }
                break;
            }
            case "GetServer": {
                final String server = in.readUTF();
                currentServer = server;
                if (!servers.containsKey(server.toUpperCase().trim())) {
                    servers.put(server.toUpperCase().trim(), new BungeeServerInfo(server));
                }

                final BungeeServerInfo info = servers.get(server.toUpperCase().trim());
                info.setMaxPlayers(Bukkit.getMaxPlayers());
                info.setPlayerCount(Bukkit.getOnlinePlayers().size());

                int onlinePlayers = 0;
                if (servers.containsKey("ALL")) {
                    onlinePlayers = servers.get("ALL").getPlayerCount();
                } else {
                    for (final BungeeServerInfo serverInfo : servers.values()) {
                        onlinePlayers += serverInfo.getPlayerCount();
                    }
                }
                this.onlinePlayers = onlinePlayers;

                break;
            }
            case "PlayerCount": {
                final String server = in.readUTF();
                final int playerCount = in.readInt();

                if (!servers.containsKey(server.toUpperCase().trim())) {
                    servers.put(server.toUpperCase().trim(), new BungeeServerInfo(server));
                }

                servers.get(server.toUpperCase().trim()).setPlayerCount(playerCount);

                int onlinePlayers = 0;
                if (servers.containsKey("ALL")) {
                    onlinePlayers = servers.get("ALL").getPlayerCount();
                } else {
                    for (final BungeeServerInfo serverInfo : servers.values()) {
                        onlinePlayers += serverInfo.getPlayerCount();
                    }
                }
                this.onlinePlayers = onlinePlayers;

                break;
            }
        }
    }

    private ByteArrayDataOutput createOutput() {
        return ByteStreams.newDataOutput();
    }

    public void sendBungeeMessage(final String... args) {
        val players = Bukkit.getOnlinePlayers().iterator();

        if (players.hasNext()) {
            sendBungeeMessage(players.next(), args);
        }
    }

    public void sendBungeeMessage(final Player player, final String... args) {
        val output = createOutput();
        for (val arg : args) {
            output.writeUTF(arg);
        }
        player.sendPluginMessage(TitleManager.getInstance(), "BungeeCord", output.toByteArray());
    }

    public void sendServerMessage(final String serverName, final String... args) {
        val players = Bukkit.getOnlinePlayers().iterator();

        if (players.hasNext()) {
            sendServerMessage(players.next(), serverName, args);
        }
    }

    public void sendServerMessage(final Player player, final String serverName, final String... args) {
        val output = createOutput();

        output.writeUTF("Forward");
        output.writeUTF(serverName);
        output.writeUTF("TitleManager");

        final ByteArrayDataOutput msg = createOutput();

        for (val arg : args) {
            msg.writeUTF(arg);
        }

        output.writeShort(msg.toByteArray().length);
        output.write(msg.toByteArray());

        player.sendPluginMessage(TitleManager.getInstance(), "BungeeCord", output.toByteArray());
    }

    public void broadcastBungeeMessage(final String... args) {
        val players = Bukkit.getOnlinePlayers().iterator();

        if (players.hasNext()) {
            broadcastBungeeMessage(players.next(), args);
        }
    }

    public void broadcastBungeeMessage(final Player player, final String... args) {
        val output = createOutput();
        val internal = createOutput();

        output.writeUTF("Forward");
        output.writeUTF("ALL");
        output.writeUTF("TitleManager");
        internal.writeUTF("TitleManager");

        final ByteArrayDataOutput msg = createOutput();

        for (val arg : args) {
            msg.writeUTF(arg);
        }

        internal.writeShort(msg.toByteArray().length);
        internal.write(msg.toByteArray());
        output.writeShort(msg.toByteArray().length);
        output.write(msg.toByteArray());

        player.sendPluginMessage(TitleManager.getInstance(), "BungeeCord", output.toByteArray());
        onPluginMessageReceived("BungeeCord", player, internal.toByteArray());
    }

    public Map<String, BungeeServerInfo> getServers() {
        return servers;
    }

    public int getOnlinePlayers() {
        return onlinePlayers;
    }

    public BungeeServerInfo getCurrentServer() {
        return currentServer == null ? null : servers.get(currentServer.toUpperCase().trim());
    }

    public Gson getGson() {
        return gson;
    }
}
