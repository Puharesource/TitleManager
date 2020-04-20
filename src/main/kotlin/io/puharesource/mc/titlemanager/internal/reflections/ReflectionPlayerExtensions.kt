package io.puharesource.mc.titlemanager.internal.reflections

import org.bukkit.entity.Player

private val classCraftPlayer = CraftPlayer()
private val classEntityPlayer = EntityPlayer()
private val classPlayerConnection = PlayerConnection()
private val classNetworkManager = NetworkManager()

internal fun Player.getPing(): Int {
    return classEntityPlayer.ping.getInt(getEntityPlayer())
}

/**
 * Checks if the player is on 1.7 on the Protocol Hack version of Spigot
 */
internal fun Player.isUsing17(): Boolean {
    if (NMSManager.versionIndex != 0) {
        return false
    }

    return classNetworkManager.getVersion(getNMSNetworkManager()) != 47
}

internal fun Player.sendNMSPacket(packet: Any) {
    if (isUsing17()) {
        return
    }

    if (NMSManager.versionIndex <= 2) {
        classPlayerConnection.sendPacket(getNMSPlayerConnection(), packet)
    } else {
        classNetworkManager.sendPacket(getNMSNetworkManager(), packet)
    }
}

internal fun Player.getEntityPlayer(): Any? {
    val instance = classCraftPlayer.clazz.handle.cast(this)

    return classCraftPlayer.getHandle(instance)
}

internal fun Player.getNMSPlayerConnection(): Any {
    return classEntityPlayer.playerConnection.get(getEntityPlayer())
}

internal fun Player.getNMSNetworkManager(): Any {
    return classPlayerConnection.networkManager.get(getNMSPlayerConnection())
}
