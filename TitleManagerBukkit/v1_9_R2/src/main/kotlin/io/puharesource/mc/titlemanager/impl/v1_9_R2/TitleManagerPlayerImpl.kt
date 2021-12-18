package io.puharesource.mc.titlemanager.impl.v1_9_R2

import io.puharesource.mc.common.NmsImplementation
import io.puharesource.mc.common.TitleManagerPlayer
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer
import java.util.UUID

@NmsImplementation(version = "v1_9_R2")
class TitleManagerPlayerImpl(player: CraftPlayer) : TitleManagerPlayer<CraftPlayer>(player) {
    override val id: UUID
        get() = handle.uniqueId

    override val ping: Int
        get() = handle.handle.ping
}
