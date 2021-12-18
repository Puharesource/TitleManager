package io.puharesource.mc.titlemanager.impl.v1_9_R2

import io.netty.buffer.Unpooled
import io.puharesource.mc.common.NmsImplementation
import io.puharesource.mc.common.TitleManagerPlayer
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.minecraft.server.v1_9_R2.ChatComponentText
import net.minecraft.server.v1_9_R2.PacketDataSerializer
import net.minecraft.server.v1_9_R2.PacketPlayOutPlayerListHeaderFooter
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer
import java.util.UUID

@NmsImplementation(version = "v1_9_R2")
class TitleManagerPlayerImpl(player: CraftPlayer) : TitleManagerPlayer<CraftPlayer>(player) {
    override val id: UUID
        get() = handle.uniqueId

    override val ping: Int
        get() = handle.handle.ping

    override var playerListHeader: String = ""
        set(value) {
            if (field != value) {
                field = value

                sendTabHeaderPacket(value, playerListFooter)
            }
        }

    override var playerListFooter: String = ""
        set(value) {
            if (field != value) {
                field = value

                sendTabHeaderPacket(playerListHeader, value)
            }
        }

    override fun sendActionbarMessage(message: String) {
        handle.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(message))
    }

    private fun sendTabHeaderPacket(header: String, footer: String) {
        val serializer = PacketDataSerializer(Unpooled.buffer())
        serializer.a(ChatComponentText(header))
        serializer.a(ChatComponentText(footer))

        val packet = PacketPlayOutPlayerListHeaderFooter()
        packet.a(serializer)

        handle.handle.playerConnection.sendPacket(packet)
    }
}
