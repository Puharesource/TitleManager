package io.puharesource.mc.titlemanager.internal.reflections

import io.puharesource.mc.titlemanager.internal.extensions.modify
import org.bukkit.entity.Player
import java.lang.reflect.Constructor

object NMSUtil {
    private val classPacketTabHeader by lazy { PacketTabHeader() }
    private val classPacketTitle by lazy { PacketTitle() }
    private val classPacketPlayOutChat by lazy { PacketPlayOutChat() }

    fun sendTimings(player: Player, fadeIn: Int, stay: Int, fadeOut: Int) {
        val packet = if (NMSManager.versionIndex == 0) {
            classPacketTitle.timingsConstructor.newInstance(TitleTypeMapper.TIMES.handle, fadeIn, stay, fadeOut)
        } else {
            classPacketTitle.constructor.newInstance(TitleTypeMapper.TIMES.handle, null, fadeIn, stay, fadeOut)
        }

        player.sendNMSPacket(packet)
    }

    fun sendTitle(player: Player, title: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        val provider = NMSManager.getClassProvider()
        val packetConstructor: Constructor<*> = classPacketTitle.constructor

        sendTimings(player, fadeIn, stay, fadeOut)

        val packet = packetConstructor
            .newInstance(
                TitleTypeMapper.TITLE.handle,
                provider.getIChatComponent(title),
                fadeIn,
                stay,
                fadeOut
            )

        player.sendNMSPacket(packet)
    }

    fun sendSubtitle(player: Player, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        val provider = NMSManager.getClassProvider()
        val packetConstructor = classPacketTitle.constructor

        val packet = packetConstructor
            .newInstance(
                TitleTypeMapper.SUBTITLE.handle,
                provider.getIChatComponent(subtitle),
                fadeIn,
                stay,
                fadeOut
            )

        player.sendNMSPacket(packet)
    }

    fun sendActionbar(player: Player, text: String) {
        val provider = NMSManager.getClassProvider()

        if (NMSManager.versionIndex == 0) {
            try {
                val packet = classPacketPlayOutChat.constructor.newInstance(provider.getIChatComponent(text), 2)

                player.sendNMSPacket(packet)
            } catch (e: NoSuchMethodException) {
                error("(If you're using Spigot #1649) Your version of Spigot #1649 doesn't support actionbar messages. Please find that spigot version from another source!")
            }
        } else {
            val packet = classPacketPlayOutChat.constructor.newInstance(provider.getIChatComponent(text), 2.toByte())

            player.sendNMSPacket(packet)
        }
    }

    fun setHeaderAndFooter(player: Player, header: String, footer: String) {
        val provider = NMSManager.getClassProvider()
        val packet: Any

        if (NMSManager.versionIndex == 0) {
            packet = classPacketTabHeader.legacyConstructor.newInstance(provider.getIChatComponent(header), provider.getIChatComponent(footer))
        } else {
            packet = classPacketTabHeader.createInstance()

            classPacketTabHeader.headerField.modify { set(packet, provider.getIChatComponent(header)) }
            classPacketTabHeader.footerField.modify { set(packet, provider.getIChatComponent(footer)) }
        }

        player.sendNMSPacket(packet)
    }
}
