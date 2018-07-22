package io.puharesource.mc.titlemanager.reflections

import org.bukkit.entity.Player

internal fun Player.getPing() : Int {
    val entityPlayer = getEntityPlayer()
    val pingField = entityPlayer.javaClass.getField("ping")

    return pingField.getInt(entityPlayer)
}

/**
 * Checks if the player is on 1.7 on the Protocol Hack version of Spigot
 */
internal fun Player.isUsing17() : Boolean {
    if (NMSManager.versionIndex != 0) {
        return false
    }

    val playerConnection = getNMSPlayerConnection()
    val networkManager = playerConnection.javaClass.getField("networkManager").get(playerConnection)

    return networkManager.javaClass.getMethod("getVersion").invoke(networkManager) as Int != 47
}

internal fun Player.sendNMSPacket(packet: Any) {
    if (isUsing17()) {
        return
    }

    val provider = NMSManager.getClassProvider()

    if (NMSManager.versionIndex <= 2) {
        val connection = getNMSPlayerConnection()

        provider.get("PlayerConnection").getMethod("sendPacket", provider.get("Packet").handle).invoke(connection, packet)
    } else {
        val networkManager = getNMSNetworkManager()

        networkManager.javaClass
                .getMethod("sendPacket", provider.get("Packet").handle)
                .invoke(networkManager, packet)
    }
}

internal fun Player.getEntityPlayer() : Any {
    val craftPlayerClass = NMSManager.getClassProvider().get("CraftPlayer")
    val instance = craftPlayerClass.handle.cast(this)

    return craftPlayerClass.getMethod("getHandle").invoke(instance)
}

internal fun Player.getNMSPlayerConnection() : Any {
    val entityPlayer = getEntityPlayer()

    return entityPlayer.javaClass.getField("playerConnection").get(entityPlayer)
}

internal fun Player.getNMSNetworkManager() : Any {
    val playerConnection = getNMSPlayerConnection()

    return playerConnection.javaClass.getField("networkManager").get(playerConnection)
}