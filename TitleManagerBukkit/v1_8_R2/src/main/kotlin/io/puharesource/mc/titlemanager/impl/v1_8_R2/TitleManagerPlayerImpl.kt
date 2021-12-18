package io.puharesource.mc.titlemanager.impl.v1_8_R2

import io.puharesource.mc.common.NmsImplementation
import io.puharesource.mc.common.TitleManagerPlayer
import net.minecraft.server.v1_8_R2.ChatComponentText
import net.minecraft.server.v1_8_R2.PacketPlayOutChat
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer
import java.util.UUID

@NmsImplementation(version = "v1_8_R2")
class TitleManagerPlayerImpl(player: CraftPlayer) : TitleManagerPlayer<CraftPlayer>(player) {
    override val id: UUID
        get() = handle.uniqueId

    override val ping: Int
        get() = handle.handle.ping

    override fun sendActionbarMessage(message: String) {
        val component = ChatComponentText(message)
        val packet = PacketPlayOutChat(component)

        handle.handle.playerConnection.sendPacket(packet)
    }
}
