package io.puharesource.mc.titlemanager.backend.player

import io.puharesource.mc.titlemanager.TitleManager
import io.puharesource.mc.titlemanager.backend.packet.Packet
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionClass
import org.bukkit.entity.Player

final class TMPlayer {

    private final Player player

    TMPlayer(final Player player) {
        this.player = player
    }

    void sendPacket(final Packet packet) {
        Object connection = getPlayerConnection()
        TitleManager.reflectionManager.classes["PlayerConnection"]
                .getMethod("sendPacket", connection.getClass(), TitleManager.reflectionManager.classes["Packet"].handle.getClass())
                .invoke(connection, packet.getHandle())
    }

    private Object getEntityPlayer() {
        ReflectionClass craftPlayerClass = TitleManager.reflectionManager.classes["CraftPlayer"];

        return craftPlayerClass.getMethod("getHandle").invoke(craftPlayerClass.handle.cast(player))
    }

    private Object getPlayerConnection() {
        TitleManager.reflectionManager.classes["EntityPlayer"].getField("playerConnection").get(getEntityPlayer())
    }
}
