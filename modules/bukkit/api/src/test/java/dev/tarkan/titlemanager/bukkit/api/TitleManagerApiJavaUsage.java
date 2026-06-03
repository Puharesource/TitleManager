package dev.tarkan.titlemanager.bukkit.api;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

final class TitleManagerApiJavaUsage {
    private TitleManagerApiJavaUsage() {
    }

    static void useService(Plugin plugin, Player player) {
        TitleManagerApi api = TitleManagerServices.require(plugin);

        try (TitleManagerSession session = api.sendActionbar(player, "Hello from Java")) {
            if (session.getType() != TitleManagerSessionType.ACTIONBAR) {
                throw new IllegalStateException("Unexpected session type");
            }
            if (session.isClosed()) {
                throw new IllegalStateException("New session should be open");
            }
        }

        api.clearActionbar(player);
    }
}
