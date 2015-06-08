package io.puharesource.mc.titlemanager.backend.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.puharesource.mc.titlemanager.TitleManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class BungeeManager implements PluginMessageListener {

    private Map<String, BungeeServerInfo> servers = new ConcurrentHashMap<>();
    private String currentServer;

    private int onlinePlayers = -1;

    public BungeeManager() {
        Bukkit.getScheduler().runTaskTimer(TitleManager.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (TitleManager.getInstance().getConfigManager().getConfig().usingBungeecord) {
                    sendMessage("GetServers");
                }
            }
        }, 0l, 200l);
    }

    @Override
    public void onPluginMessageReceived(final String channel, final Player player, final byte[] message) {
        if (!TitleManager.getInstance().getConfigManager().getConfig().usingBungeecord) return;
        if (!channel.equals("BungeeCord")) return;

        final ByteArrayDataInput in = ByteStreams.newDataInput(message);
        final String subChannel = in.readUTF();

        switch (subChannel) {
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

    public void sendMessage(final String... args) {
        final Iterator<? extends Player> players = Bukkit.getOnlinePlayers().iterator();

        if (players.hasNext()) {
            sendMessage(players.next(), args);
        }
    }

    public void sendMessage(final Player player, final String... args) {
        ByteArrayDataOutput output = createOutput();
        for (final String arg : args) {
            output.writeUTF(arg);
        }
        player.sendPluginMessage(TitleManager.getInstance(), "BungeeCord", output.toByteArray());
    }

    public Map<String, BungeeServerInfo> getServers() {
        return servers;
    }

    public int getOnlinePlayers() {
        return onlinePlayers;
    }

    public BungeeServerInfo getCurrentServer() {
        return servers.get(currentServer);
    }
}
