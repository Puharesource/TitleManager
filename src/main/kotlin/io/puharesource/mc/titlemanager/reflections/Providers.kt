package io.puharesource.mc.titlemanager.reflections

import io.puharesource.mc.titlemanager.reflections.NMSType.*

/**
 * Provides classes for Minecraft 1.7 - 1.8 Protocol Hack
 */
object ProviderProtocolHack : NMSClassProvider() {
    init {
        put("ChatComponentText",                        NET_MINECRAFT_SERVER.getReflectionClass("ChatComponentText"))
        put("IChatBaseComponent",                       NET_MINECRAFT_SERVER.getReflectionClass("IChatBaseComponent"))
        put("CraftPlayer",                              ORG_BUKKIT_CRAFTBUKKIT.getReflectionClass("entity.CraftPlayer"))
        put("EntityPlayer",                             NET_MINECRAFT_SERVER.getReflectionClass("EntityPlayer"))
        put("PlayerConnection",                         NET_MINECRAFT_SERVER.getReflectionClass("PlayerConnection"))
        put("NetworkManager",                           NET_MINECRAFT_SERVER.getReflectionClass("NetworkManager"))
        put("Packet",                                   NET_MINECRAFT_SERVER.getReflectionClass("Packet"))
        put("PacketPlayOutChat",                        NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutChat"))
        put("PacketPlayOutSetSlot",                     NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutSetSlot"))
        put("ItemStack",                                NET_MINECRAFT_SERVER.getReflectionClass("ItemStack"))
        put("CraftItemStack",                           ORG_BUKKIT_CRAFTBUKKIT.getReflectionClass("inventory.CraftItemStack"))

        put("PacketPlayOutScoreboardObjective",         NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutScoreboardObjective"))
        put("PacketPlayOutScoreboardDisplayObjective",  NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutScoreboardDisplayObjective"))
        put("PacketPlayOutScoreboardScore",             NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutScoreboardScore"))
        put("EnumScoreboardHealthDisplay",              NET_MINECRAFT_SERVER.getReflectionClass("IScoreboardCriteria").getInnerReflectionClass("EnumScoreboardHealthDisplay"))
        put("EnumScoreboardAction",                     NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutScoreboardScore").getInnerReflectionClass("EnumScoreboardAction"))

        val protocolInjector = ORG_SPIGOTMC.getReflectionClass("ProtocolInjector")
        val packetTitle = protocolInjector.getInnerReflectionClass("PacketTitle")

        put("ProtocolInjector",         protocolInjector)
        put("PacketTitle",              packetTitle)
        put("Action",                   packetTitle.getInnerReflectionClass("Action"))
        put("PacketTabHeader",          protocolInjector.getInnerReflectionClass("PacketTabHeader"))
    }
}

/**
 * Provides classes for Minecraft 1.8 - 1.8.2
 */
object Provider18 : NMSClassProvider() {
    init {
        put("ChatComponentText",                        NET_MINECRAFT_SERVER.getReflectionClass("ChatComponentText"))
        put("IChatBaseComponent",                       NET_MINECRAFT_SERVER.getReflectionClass("IChatBaseComponent"))
        put("CraftPlayer",                              ORG_BUKKIT_CRAFTBUKKIT.getReflectionClass("entity.CraftPlayer"))
        put("EntityPlayer",                             NET_MINECRAFT_SERVER.getReflectionClass("EntityPlayer"))
        put("PlayerConnection",                         NET_MINECRAFT_SERVER.getReflectionClass("PlayerConnection"))
        put("NetworkManager",                           NET_MINECRAFT_SERVER.getReflectionClass("NetworkManager"))
        put("Packet",                                   NET_MINECRAFT_SERVER.getReflectionClass("Packet"))
        put("PacketPlayOutTitle",                       NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutTitle"))
        put("PacketPlayOutChat",                        NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutChat"))
        put("PacketPlayOutPlayerListHeaderFooter",      NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutPlayerListHeaderFooter"))
        put("EnumTitleAction",                          NET_MINECRAFT_SERVER.getReflectionClass("EnumTitleAction"))
        put("PacketPlayOutScoreboardObjective",         NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutScoreboardObjective"))
        put("PacketPlayOutScoreboardDisplayObjective",  NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutScoreboardDisplayObjective"))
        put("PacketPlayOutScoreboardScore",             NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutScoreboardScore"))
        put("EnumScoreboardHealthDisplay",              NET_MINECRAFT_SERVER.getReflectionClass("IScoreboardCriteria").getInnerReflectionClass("EnumScoreboardHealthDisplay"))
        put("EnumScoreboardAction",                     NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutScoreboardScore").getInnerReflectionClass("EnumScoreboardAction"))
    }
}

/**
 * Provides classes for Minecraft 1.8.3 - 1.9.X
 */
object Provider183 : NMSClassProvider() {
    init {
        put("ChatComponentText",                        NET_MINECRAFT_SERVER.getReflectionClass("ChatComponentText"))
        put("IChatBaseComponent",                       NET_MINECRAFT_SERVER.getReflectionClass("IChatBaseComponent"))
        put("CraftPlayer",                              ORG_BUKKIT_CRAFTBUKKIT.getReflectionClass("entity.CraftPlayer"))
        put("EntityPlayer",                             NET_MINECRAFT_SERVER.getReflectionClass("EntityPlayer"))
        put("PlayerConnection",                         NET_MINECRAFT_SERVER.getReflectionClass("PlayerConnection"))
        put("NetworkManager",                           NET_MINECRAFT_SERVER.getReflectionClass("NetworkManager"))
        put("Packet",                                   NET_MINECRAFT_SERVER.getReflectionClass("Packet"))
        put("PacketPlayOutTitle",                       NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutTitle"))
        put("PacketPlayOutChat",                        NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutChat"))
        put("PacketPlayOutPlayerListHeaderFooter",      NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutPlayerListHeaderFooter"))
        put("EnumTitleAction",                          NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutTitle").getInnerReflectionClass("EnumTitleAction"))
        put("PacketPlayOutScoreboardObjective",         NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutScoreboardObjective"))
        put("PacketPlayOutScoreboardDisplayObjective",  NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutScoreboardDisplayObjective"))
        put("PacketPlayOutScoreboardScore",             NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutScoreboardScore"))
        put("EnumScoreboardHealthDisplay",              NET_MINECRAFT_SERVER.getReflectionClass("IScoreboardCriteria").getInnerReflectionClass("EnumScoreboardHealthDisplay"))
        put("EnumScoreboardAction",                     NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutScoreboardScore").getInnerReflectionClass("EnumScoreboardAction"))
    }
}

/**
 * Provides classes for Minecraft 1.10 - 1.11
 */
object Provider110 : NMSClassProvider() {
    init {
        put("ChatComponentText",                        NET_MINECRAFT_SERVER.getReflectionClass("ChatComponentText"))
        put("IChatBaseComponent",                       NET_MINECRAFT_SERVER.getReflectionClass("IChatBaseComponent"))
        put("CraftPlayer",                              ORG_BUKKIT_CRAFTBUKKIT.getReflectionClass("entity.CraftPlayer"))
        put("EntityPlayer",                             NET_MINECRAFT_SERVER.getReflectionClass("EntityPlayer"))
        put("PlayerConnection",                         NET_MINECRAFT_SERVER.getReflectionClass("PlayerConnection"))
        put("NetworkManager",                           NET_MINECRAFT_SERVER.getReflectionClass("NetworkManager"))
        put("Packet",                                   NET_MINECRAFT_SERVER.getReflectionClass("Packet"))
        put("PacketPlayOutTitle",                       NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutTitle"))
        put("PacketPlayOutChat",                        NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutChat"))
        put("PacketPlayOutPlayerListHeaderFooter",      NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutPlayerListHeaderFooter"))
        put("EnumTitleAction",                          NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutTitle").getInnerReflectionClass("EnumTitleAction"))
        put("PacketPlayOutScoreboardObjective",         NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutScoreboardObjective"))
        put("PacketPlayOutScoreboardDisplayObjective",  NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutScoreboardDisplayObjective"))
        put("PacketPlayOutScoreboardScore",             NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutScoreboardScore"))
        put("EnumScoreboardHealthDisplay",              NET_MINECRAFT_SERVER.getReflectionClass("IScoreboardCriteria").getInnerReflectionClass("EnumScoreboardHealthDisplay"))
        put("EnumScoreboardAction",                     NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutScoreboardScore").getInnerReflectionClass("EnumScoreboardAction"))
    }
}