package io.puharesource.mc.titlemanager.internal.model.bungeecord

import io.puharesource.mc.titlemanager.internal.services.bungeecord.BungeeCordService

data class BungeeCordServerInfo(val name: String, var playerCount: Int = 0, var maxPlayers: Int = 0) {
    fun update(bungeeCordService: BungeeCordService) = bungeeCordService.sendNetworkMessage("PlayerCount", name)
}
