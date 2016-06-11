package io.puharesource.mc.titlemanager.backend.bungee;

import lombok.Getter;

public enum BungeeChannel {
    TITLE_BROADCAST("TITLE_OBJECT_BROADCAST"),
    TITLE_MESSAGE("TITLE_OBJECT_MESSAGE"),
    ACTIONBAR_BROADCAST("ACTIONBAR_BROADCAST"),
    ACTIONBAR_MESSAGE("ACTIONBAR_MESSAGE");

    @Getter private final String channel;

    BungeeChannel(final String channel) {
        this.channel = channel;
    }
}
