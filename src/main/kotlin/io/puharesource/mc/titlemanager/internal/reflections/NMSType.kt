package io.puharesource.mc.titlemanager.internal.reflections

enum class NMSType(private val path: String) {
    NET_MINECRAFT_SERVER("net.minecraft.server"),
    ORG_BUKKIT_CRAFTBUKKIT("org.bukkit.craftbukkit"),
    ORG_SPIGOTMC("org.spigotmc");

    fun getReflectionClass(path: String): ReflectionClass {
        if (this != ORG_SPIGOTMC) {
            val version = NMSManager.serverVersion
            return ReflectionClass("${this.path}.$version.$path")
        }

        return ReflectionClass("${this.path}.$path")
    }
}
