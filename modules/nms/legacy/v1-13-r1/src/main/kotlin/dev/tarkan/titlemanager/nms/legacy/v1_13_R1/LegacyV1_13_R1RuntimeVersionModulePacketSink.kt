package dev.tarkan.titlemanager.nms.legacy.v1_13_R1

import dev.tarkan.titlemanager.nms.legacy.LegacyDirectNmsPacketSink
import dev.tarkan.titlemanager.nms.legacy.LegacyTitleTicks
import dev.tarkan.titlemanager.nms.legacy.legacyComponentJson
import net.minecraft.server.v1_13_R1.IChatBaseComponent
import net.minecraft.server.v1_13_R1.ChatMessageType
import net.minecraft.server.v1_13_R1.Packet
import net.minecraft.server.v1_13_R1.PacketPlayOutChat
import net.minecraft.server.v1_13_R1.PacketPlayOutPlayerListHeaderFooter
import net.minecraft.server.v1_13_R1.PacketPlayOutTitle
import org.bukkit.entity.Player
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer
import java.lang.reflect.Field

object LegacyV1_13_R1RuntimeVersionModulePacketSink : LegacyDirectNmsPacketSink {
    override fun sendTitleTimes(player: Player, ticks: LegacyTitleTicks) {
        sendPacket(player, PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, null, ticks.fadeIn, ticks.stay, ticks.fadeOut))
    }

    override fun sendTitle(player: Player, title: String, ticks: LegacyTitleTicks) {
        sendPacket(player, PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, component(title), ticks.fadeIn, ticks.stay, ticks.fadeOut))
    }

    override fun sendSubtitle(player: Player, subtitle: String, ticks: LegacyTitleTicks) {
        sendPacket(player, PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, component(subtitle), ticks.fadeIn, ticks.stay, ticks.fadeOut))
    }

    override fun sendActionbar(player: Player, text: String) {
        sendPacket(player, PacketPlayOutChat(component(text), ChatMessageType.GAME_INFO))
    }

    override fun sendPlayerListHeaderAndFooter(player: Player, header: String, footer: String) {
        sendPacket(player, PlayerListPacketFields.packet(component(header), component(footer)))
    }

    private fun component(text: String): IChatBaseComponent = IChatBaseComponent.ChatSerializer.a(legacyComponentJson(text))!!

    private fun sendPacket(player: Player, packet: Packet<*>) {
        (player as CraftPlayer).handle.playerConnection.sendPacket(packet)
    }

    private object PlayerListPacketFields {
        private val headerField = packetField("a")
        private val footerField = packetField("b")

        fun packet(header: IChatBaseComponent, footer: IChatBaseComponent): PacketPlayOutPlayerListHeaderFooter {
            return PacketPlayOutPlayerListHeaderFooter().also { packet ->
                headerField.set(packet, header)
                footerField.set(packet, footer)
            }
        }

        private fun packetField(name: String): Field {
            return PacketPlayOutPlayerListHeaderFooter::class.java.getDeclaredField(name).apply { isAccessible = true }
        }
    }
}
