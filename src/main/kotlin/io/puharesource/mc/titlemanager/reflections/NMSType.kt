package io.puharesource.mc.titlemanager.reflections

enum class NMSType {
    NET_MINECRAFT_SERVER("net.minecraft.server"),
    ORG_BUKKIT_CRAFTBUKKIT("org.bukkit.craftbukkit"),
    ORG_SPIGOTMC("org.spigotmc");

    private val path: String

    constructor(path: String) {
        this.path = path
    }

    fun getReflectionClass(path: String): ReflectionClass {
        if (this != ORG_SPIGOTMC) {
            val version = NMSManager.serverVersion
            return ReflectionClass("${this.path}.$version.$path")
        }

        return ReflectionClass("${this.path}.$path")
    }
}