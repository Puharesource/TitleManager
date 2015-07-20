package io.puharesource.mc.titlemanager.backend.player;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.backend.packet.ActionbarTitlePacket;
import io.puharesource.mc.titlemanager.backend.packet.Packet;
import io.puharesource.mc.titlemanager.backend.packet.v1_7.ItemMessagePacket;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionClass;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;
import io.puharesource.mc.titlemanager.backend.reflections.managers.ReflectionManagerProtocolHack1718;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public final class TMPlayer implements Comparable<TMPlayer> {
    private final Player player;
    private final Object entityPlayer;
    private final Field pingField;

    @SneakyThrows
    public TMPlayer(final Player player) {
        this.player = player;

        val craftPlayerClass = ReflectionManager.ReflectionType.ORG_BUKKIT_CRAFTBUKKIT.getReflectionClass("entity.CraftPlayer");
        val craftPlayer = craftPlayerClass.getHandle().cast(player);
        val handle = craftPlayer.getClass().getMethod("getHandle");

        entityPlayer = handle.invoke(craftPlayer);
        pingField = entityPlayer.getClass().getField("ping");
    }

    @SneakyThrows
    public void sendPacket(Packet packet) {
        if (isUsing17() && TitleManager.getInstance().getConfigManager().getConfig().legacyClientSupport) {
            if (packet instanceof ActionbarTitlePacket) {
                packet = new ItemMessagePacket(((ActionbarTitlePacket) packet).getText(), player, player.getInventory().getHeldItemSlot());
            } else if (!(packet instanceof ItemMessagePacket)) {
                packet = null;
            }
        } else {
            if (packet instanceof ItemMessagePacket) {
                packet = null;
            }
        }

        if (packet != null && packet.getHandle() != null) {
            Object connection = getPlayerConnection();
            ReflectionManager reflectionManager = TitleManager.getInstance().getReflectionManager();

            reflectionManager.getClasses().get("PlayerConnection").getMethod("sendPacket", reflectionManager.getClasses().get("Packet").getHandle()).invoke(connection, packet.getHandle());
        }
    }

    @SneakyThrows
    private Object getEntityPlayer() {
        ReflectionClass craftPlayerClass = TitleManager.getInstance().getReflectionManager().getClasses().get("CraftPlayer");

        return craftPlayerClass.getMethod("getHandle").invoke(craftPlayerClass.getHandle().cast(player));
    }

    @SneakyThrows
    private Object getPlayerConnection() {
        return entityPlayer.getClass().getField("playerConnection").get(getEntityPlayer());
    }

    @SneakyThrows
    public boolean isUsing17() {
        if (!(TitleManager.getInstance().getReflectionManager() instanceof ReflectionManagerProtocolHack1718)) return false;

        Object playerConnection = getPlayerConnection();
        Object networkManager = playerConnection.getClass().getField("networkManager").get(playerConnection);

        return (int) networkManager.getClass().getMethod("getVersion").invoke(networkManager) != 47;
    }

    @SneakyThrows
    public int getPing() {
        return pingField.getInt(entityPlayer);
    }

    @Override
    public int compareTo(@NonNull TMPlayer o) {
        return player.getName().compareToIgnoreCase(o.player.getName());
    }
}
