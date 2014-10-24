package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.events.ActionbarEvent;
import net.minecraft.server.v1_7_R4.ChatSerializer;
import net.minecraft.server.v1_7_R4.IChatBaseComponent;
import net.minecraft.server.v1_7_R4.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ActionbarTitleObject {
    private String rawTitle;
    private IChatBaseComponent title;

    public ActionbarTitleObject(String title) {
        setTitle(title);
    }

    public void send(Player p) {
        final ActionbarEvent event = new ActionbarEvent(p, this);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        CraftPlayer player = (CraftPlayer) p;
        if (player.getHandle().playerConnection.networkManager.getVersion() != TitleManager.PROTOCOL_VERSION) return;
        PacketPlayOutChat packet = new PacketPlayOutChat(title, 2);
        player.getHandle().playerConnection.sendPacket(packet);
    }

    public void setTitle(String title) {
        rawTitle = title;
        this.title = ChatSerializer.a(TextConverter.convert(title));
    }

    public String getTitle() {
        return rawTitle;
    }
}
