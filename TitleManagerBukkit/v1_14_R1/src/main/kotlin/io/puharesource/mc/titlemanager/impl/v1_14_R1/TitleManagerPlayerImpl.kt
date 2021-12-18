package io.puharesource.mc.titlemanager.impl.v1_14_R1

import io.puharesource.mc.common.NmsImplementation
import io.puharesource.mc.common.TitleManagerPlayer
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.minecraft.server.v1_14_R1.PacketPlayOutTitle
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import java.util.UUID

@NmsImplementation(version = "v1_14_R1")
class TitleManagerPlayerImpl(player: CraftPlayer) : TitleManagerPlayer<CraftPlayer>(player) {
    override val id: UUID
        get() = handle.uniqueId

    override val ping: Int
        get() = handle.handle.ping

    override var playerListHeader: String
        get() = handle.playerListHeader.orEmpty()
        set(value) {
            handle.playerListHeader = value
        }

    override var playerListFooter: String
        get() = handle.playerListFooter.orEmpty()
        set(value) {
            handle.playerListFooter = value
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
}
