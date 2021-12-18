package io.puharesource.mc.titlemanager.impl.v1_18_R1

import io.puharesource.mc.common.NmsImplementation
import io.puharesource.mc.common.TitleManagerPlayer
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer
import java.util.UUID

@NmsImplementation(version = "v1_18_R1")
class TitleManagerPlayerImpl(player: CraftPlayer) : TitleManagerPlayer<CraftPlayer>(player) {
    override val id: UUID
        get() = handle.uniqueId

    override val ping: Int
        get() = handle.ping
}
