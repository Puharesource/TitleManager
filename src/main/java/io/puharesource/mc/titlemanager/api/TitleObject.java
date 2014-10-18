package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.events.TitleEvent;
import net.minecraft.server.v1_7_R4.ChatSerializer;
import net.minecraft.server.v1_7_R4.IChatBaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.spigotmc.ProtocolInjector;

public class TitleObject {

    private String rawTitle;
    private String rawSubtitle;

    private IChatBaseComponent title;
    private IChatBaseComponent subtitle;

    private int fadeIn = -1;
    private int stay = -1;
    private int fadeOut = -1;

    public TitleObject(String title, TitleType type) {
        IChatBaseComponent serializedTitle = ChatSerializer.a(TextConverter.convert(title));
        if (type == TitleType.TITLE) {
            rawTitle = title;
            this.title = serializedTitle;
        } else if (type == TitleType.SUBTITLE) {
            rawSubtitle = title;
            subtitle = serializedTitle;
        }
    }

    public TitleObject(String title, String subtitle) {
        rawTitle = title;
        rawSubtitle = subtitle;
        this.title = ChatSerializer.a(TextConverter.convert(title));
        this.subtitle = ChatSerializer.a(TextConverter.convert(subtitle));
    }

    public void send(Player p) {
        final TitleEvent event = new TitleEvent(p, this);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        CraftPlayer player = (CraftPlayer) event.getPlayer();
        if (player.getHandle().playerConnection.networkManager.getVersion() != TitleManager.PROTOCOL_VERSION) return;

        player.getHandle().playerConnection.sendPacket(new ProtocolInjector.PacketTitle(ProtocolInjector.PacketTitle.Action.TIMES, fadeIn, stay, fadeOut));
        if (title != null)
            player.getHandle().playerConnection.sendPacket(new ProtocolInjector.PacketTitle(ProtocolInjector.PacketTitle.Action.TITLE, title));
        if (subtitle != null)
            player.getHandle().playerConnection.sendPacket(new ProtocolInjector.PacketTitle(ProtocolInjector.PacketTitle.Action.SUBTITLE, subtitle));
    }

    public String getTitle() {
        return rawTitle;
    }

    public TitleObject setTitle(String title) {
        rawTitle = title;
        this.title = ChatSerializer.a(TextConverter.convert(title));
        return this;
    }

    public String getSubtitle() {
        return rawSubtitle;
    }

    public TitleObject setSubtitle(String subtitle) {
        rawSubtitle = subtitle;
        this.subtitle = ChatSerializer.a(TextConverter.convert(subtitle));
        return this;
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public TitleObject setFadeIn(int i) {
        fadeIn = i;
        return this;
    }

    public int getStay() {
        return stay;
    }

    public TitleObject setStay(int i) {
        stay = i;
        return this;
    }

    public int getFadeOut() {
        return fadeOut;
    }

    public TitleObject setFadeOut(int i) {
        fadeOut = i;
        return this;
    }

    public enum TitleType {TITLE, SUBTITLE}
}