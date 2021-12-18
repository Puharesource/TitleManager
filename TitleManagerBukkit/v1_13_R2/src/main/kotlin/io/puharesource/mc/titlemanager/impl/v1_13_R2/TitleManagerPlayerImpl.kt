package io.puharesource.mc.titlemanager.impl.v1_13_R2

import io.puharesource.mc.common.NmsImplementation
import io.puharesource.mc.common.TitleManagerPlayer
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer
import java.util.UUID

@NmsImplementation(version = "v1_13_R2")
class TitleManagerPlayerImpl(player: CraftPlayer) : TitleManagerPlayer<CraftPlayer>(player) {
    override val id: UUID
        get() = handle.uniqueId

    override val ping: Int
        get() = handle.handle.ping

    override fun sendActionbarMessage(message: String) {
        handle.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(message))
    }
}
