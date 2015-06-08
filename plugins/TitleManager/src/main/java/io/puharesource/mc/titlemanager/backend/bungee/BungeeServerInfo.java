package io.puharesource.mc.titlemanager.backend.bungee;

import io.puharesource.mc.titlemanager.TitleManager;

public final class BungeeServerInfo {

    private final String name;
    private int playerCount = 0;
    private int maxPlayers = 0;

    public BungeeServerInfo(final String name) {
        this.name = name;
    }

    public void update() {
        TitleManager.getInstance().getBungeeManager().sendMessage("PlayerCount", name);
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
}
