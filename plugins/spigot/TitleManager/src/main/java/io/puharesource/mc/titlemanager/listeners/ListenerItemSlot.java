package io.puharesource.mc.titlemanager.listeners;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.backend.packet.v1_7.ItemMessagePacket;
import io.puharesource.mc.titlemanager.backend.player.TMPlayer;
import lombok.val;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

public final class ListenerItemSlot implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemHeldChange(final PlayerItemHeldEvent event) {
        if (!TitleManager.getInstance().getConfigManager().getConfig().legacyClientSupport) return;

        val player = new TMPlayer(event.getPlayer());
        if (player.isUsing17()) {
            player.sendPacket(new ItemMessagePacket(null, event.getPlayer(), event.getPreviousSlot()));
        }
    }
}
