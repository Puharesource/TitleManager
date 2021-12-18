package io.puharesource.mc.titlemanager.impl.v1_11_R1

import io.netty.buffer.Unpooled
import io.puharesource.mc.common.NmsImplementation
import io.puharesource.mc.common.TitleManagerPlayer
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.minecraft.server.v1_11_R1.ChatComponentText
import net.minecraft.server.v1_11_R1.PacketDataSerializer
import net.minecraft.server.v1_11_R1.PacketPlayOutPlayerListHeaderFooter
import net.minecraft.server.v1_11_R1.PacketPlayOutTitle
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer
import java.util.UUID

@NmsImplementation(version = "v1_11_R1")
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

    override fun sendTitle(message: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        handle.sendTitle(message, null, fadeIn, stay, fadeOut)
    }

    override fun sendSubtitle(message: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        handle.sendTitle(null, message, fadeIn, stay, fadeOut)
    }

    override fun sendTitles(title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        handle.sendTitle(title, subtitle, fadeIn, stay, fadeOut)
    }

    override fun sendTitleTimings(fadeIn: Int, stay: Int, fadeOut: Int) {
        val packet = PacketPlayOutTitle(fadeIn, stay, fadeOut)

        handle.handle.playerConnection.sendPacket(packet)
    }

    override fun resetTitle() {
        handle.resetTitle()
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
