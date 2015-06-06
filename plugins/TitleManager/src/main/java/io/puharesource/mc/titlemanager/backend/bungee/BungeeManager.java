package io.puharesource.mc.titlemanager.backend.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.puharesource.mc.titlemanager.TitleManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.*;

public final class BungeeManager implements PluginMessageListener {

    private Map<String, BungeeServerInfo> servers = new HashMap<>();

    public BungeeManager() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(TitleManager.getInstance(), new Runnable() {
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
                final List<String> newServers = Arrays.asList(in.readUTF().split(", "));

                for (final String server : servers.keySet()) {
                    if (!newServers.contains(server)) {
                        servers.remove(server);
                    }
                }

                for (final String server : newServers) {
                    if (!servers.containsKey(server)) {
                        servers.put(server, new BungeeServerInfo(server));
                    }

                    servers.get(server).update();
                }
                break;
            }
            case "GetServer": {
                final String server = in.readUTF();
                if (!servers.containsKey(server)) {
                    servers.put(server, new BungeeServerInfo(server));
                }

                final BungeeServerInfo info = servers.get(server);
                info.setMaxPlayers(Bukkit.getMaxPlayers());
                info.setPlayerCount(Bukkit.getOnlinePlayers().size());
                break;
            }
            case "PlayerCount": {
                final String server = in.readUTF();
                final int playerCount = in.readInt();

                if (!servers.containsKey(server)) {
                    servers.put(server, new BungeeServerInfo(server));
                }

                servers.get(server).setPlayerCount(playerCount);
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
}
