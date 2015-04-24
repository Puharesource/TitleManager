package io.puharesource.mc.titlemanager.backend.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public final class BungeeManager implements PluginMessageListener {

    private String[] servers;


    public BungeeManager() {

    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();

        if (subchannel.equals("GetServers")) {
            servers = in.readUTF().split(", ");
        } else if (subchannel.equals("")) {
            
        }
    }

    private ByteArrayDataOutput createOutput() {
        return ByteStreams.newDataOutput();
    }

    public String[] getServers() {
        return servers;
    }
}
