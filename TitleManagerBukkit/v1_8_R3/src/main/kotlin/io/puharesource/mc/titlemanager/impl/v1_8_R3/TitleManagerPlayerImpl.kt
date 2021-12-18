package io.puharesource.mc.titlemanager.impl.v1_8_R3

import io.puharesource.mc.common.NmsImplementation
import io.puharesource.mc.common.TitleManagerPlayer
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.minecraft.server.v1_8_R3.ChatBaseComponent
import net.minecraft.server.v1_8_R3.ChatComponentText
import net.minecraft.server.v1_8_R3.PacketPlayOutChat
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import java.util.UUID

@NmsImplementation(version = "v1_8_R3")
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
