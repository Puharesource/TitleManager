package io.puharesource.mc.titlemanager.backend.bungee;

import io.puharesource.mc.titlemanager.TitleManager;
import org.bukkit.entity.Player;

public final class BungeeServerInfo {

    private final String name;
    private int playerCount = 0;
    private int maxPlayers = 0;

    public BungeeServerInfo(final String name) {
        this.name = name;
    }

    public void update() {
        TitleManager.getInstance().getBungeeManager().sendBungeeMessage("PlayerCount", name);
    }

    public String getName() {
        return name;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void sendMessage(final String... args) {
        TitleManager.getInstance().getBungeeManager().sendServerMessage(name, args);
    }

    public void sendMessage(final Player player, final String... args) {
        TitleManager.getInstance().getBungeeManager().sendServerMessage(player, name, args);
    }
}
