package io.puharesource.mc.titlemanager.backend.player;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.backend.packet.Packet;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionClass;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;
import io.puharesource.mc.titlemanager.backend.reflections.managers.ReflectionManagerProtocolHack1718;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class TMPlayer implements Comparable<TMPlayer> {
    private final Player player;

    private ReflectionClass craftPlayerClass;
    private Object craftPlayer;
    private Object entityPlayer;
    private Field pingField;
    private Method handle;

    public TMPlayer(final Player player) {
        this.player = player;

        try {
            craftPlayerClass = ReflectionManager.ReflectionType.ORG_BUKKIT_CRAFTBUKKIT.getReflectionClass("entity.CraftPlayer");
            craftPlayer = craftPlayerClass.getHandle().cast(player);
            handle = craftPlayer.getClass().getMethod("getHandle");
            entityPlayer = handle.invoke(craftPlayer);
            pingField = entityPlayer.getClass().getField("ping");
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void sendPacket(final Packet packet) {
        if (!shouldSendPacket()) return;
        Object connection = getPlayerConnection();
        ReflectionManager reflectionManager = TitleManager.getInstance().getReflectionManager();
        try {
            reflectionManager.getClasses().get("PlayerConnection").getMethod("sendPacket", reflectionManager.getClasses().get("Packet").getHandle()).invoke(connection, packet.getHandle());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private Object getEntityPlayer() {
        ReflectionClass craftPlayerClass = TitleManager.getInstance().getReflectionManager().getClasses().get("CraftPlayer");

        try {
            return craftPlayerClass.getMethod("getHandle").invoke(craftPlayerClass.getHandle().cast(player));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object getPlayerConnection() {
        try {
            return TitleManager.getInstance().getReflectionManager().getClasses().get("EntityPlayer").getField("playerConnection").get(getEntityPlayer());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean shouldSendPacket() {
        if (!(TitleManager.getInstance().getReflectionManager() instanceof ReflectionManagerProtocolHack1718)) return true;
        try {
            Object playerConnection = getPlayerConnection();
            Object networkManager = playerConnection.getClass().getField("networkManager").get(playerConnection);
            return (int) networkManager.getClass().getMethod("getVersion").invoke(networkManager) == 47;
        } catch (NoSuchFieldException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getPing() {
        try {
            return pingField.getInt(entityPlayer);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int compareTo(TMPlayer o) {
        return player.getName().compareToIgnoreCase(o.player.getName());
    }
}
