package io.puharesource.mc.titlemanager.internal.reflections

enum class NMSType(private val path: String, private val usesVersion: Boolean = false) {
    NET_MINECRAFT_SERVER("net.minecraft.server", usesVersion = NMSManager.versionIndex < 11),
    NET_MINECRAFT_NETWORK("net.minecraft.network"),
    NET_MINECRAFT_WORLD("net.minecraft.world"),
    ORG_BUKKIT_CRAFTBUKKIT("org.bukkit.craftbukkit", usesVersion = true),
    ORG_SPIGOTMC("org.spigotmc");

    fun getReflectionClass(path: String): ReflectionClass {
        if (usesVersion) {
            val version = NMSManager.serverVersion
            return ReflectionClass("${this.path}.$version.$path")
        }

        return ReflectionClass("${this.path}.$path")
    }
}
