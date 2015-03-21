package io.puharesource.mc.titlemanager.backend.player

import io.puharesource.mc.titlemanager.TitleManager
import io.puharesource.mc.titlemanager.backend.packet.Packet
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionClass
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager
import org.bukkit.entity.Player

final class TMPlayer {

    private final Player player

    TMPlayer(final Player player) {
        this.player = player
    }

    void sendPacket(final Packet packet) {
        Object connection = getPlayerConnection()
        ReflectionManager reflectionManager = TitleManager.getInstance().getReflectionManager();
        reflectionManager.classes["PlayerConnection"]
                .getMethod("sendPacket", connection.getClass(), reflectionManager.classes["Packet"].handle.getClass())
                .invoke(connection, packet.getHandle())
    }

    private Object getEntityPlayer() {
        ReflectionClass craftPlayerClass = TitleManager.getInstance().getReflectionManager().classes["CraftPlayer"];

        return craftPlayerClass.getMethod("getHandle").invoke(craftPlayerClass.handle.cast(player))
    }

    private Object getPlayerConnection() {
        TitleManager.getInstance().getReflectionManager().classes["EntityPlayer"].getField("playerConnection").get(getEntityPlayer())
    }
}
