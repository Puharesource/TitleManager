package io.puharesource.mc.titlemanager.internal.reflections

import io.puharesource.mc.titlemanager.internal.reflections.NMSType.NET_MINECRAFT_SERVER
import io.puharesource.mc.titlemanager.internal.reflections.NMSType.ORG_BUKKIT_CRAFTBUKKIT
import io.puharesource.mc.titlemanager.internal.reflections.NMSType.ORG_SPIGOTMC

/**
 * Provides classes for Minecraft 1.7 - 1.8 Protocol Hack
 */
object ProviderProtocolHack : NMSClassProvider() {
    init {
        "ChatComponentText".associate(NET_MINECRAFT_SERVER, "ChatComponentText")
        "IChatBaseComponent".associate(NET_MINECRAFT_SERVER, "IChatBaseComponent")
        "CraftPlayer".associate(ORG_BUKKIT_CRAFTBUKKIT, "entity.CraftPlayer")
        "EntityPlayer".associate(NET_MINECRAFT_SERVER, "EntityPlayer")
        "PlayerConnection".associate(NET_MINECRAFT_SERVER, "PlayerConnection")
        "NetworkManager".associate(NET_MINECRAFT_SERVER, "NetworkManager")
        "Packet".associate(NET_MINECRAFT_SERVER, "Packet")
        "PacketPlayOutChat".associate(NET_MINECRAFT_SERVER, "PacketPlayOutChat")
        "PacketPlayOutSetSlot".associate(NET_MINECRAFT_SERVER, "PacketPlayOutSetSlot")
        "ItemStack".associate(NET_MINECRAFT_SERVER, "ItemStack")
        "CraftItemStack".associate(ORG_BUKKIT_CRAFTBUKKIT, "inventory.CraftItemStack")

        "PacketPlayOutScoreboardObjective".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardObjective")
        "PacketPlayOutScoreboardDisplayObjective".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardDisplayObjective")
        "PacketPlayOutScoreboardScore".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardScore")
        "PacketPlayOutScoreboardTeam".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardTeam")

        val protocolInjector = ORG_SPIGOTMC.getReflectionClass("ProtocolInjector")
        val packetTitle = protocolInjector.getInnerReflectionClass("PacketTitle")

        put("ProtocolInjector", protocolInjector)
        put("PacketTitle", packetTitle)
        put("Action", packetTitle.getInnerReflectionClass("Action"))
        put("PacketTabHeader", protocolInjector.getInnerReflectionClass("PacketTabHeader"))
    }
}

/**
 * Provides classes for Minecraft 1.8 - 1.8.2
 */
object Provider18 : NMSClassProvider() {
    init {
        "ChatComponentText".associate(NET_MINECRAFT_SERVER, "ChatComponentText")
        "IChatBaseComponent".associate(NET_MINECRAFT_SERVER, "IChatBaseComponent")
        "CraftPlayer".associate(ORG_BUKKIT_CRAFTBUKKIT, "entity.CraftPlayer")
        "EntityPlayer".associate(NET_MINECRAFT_SERVER, "EntityPlayer")
        "PlayerConnection".associate(NET_MINECRAFT_SERVER, "PlayerConnection")
        "NetworkManager".associate(NET_MINECRAFT_SERVER, "NetworkManager")
        "Packet".associate(NET_MINECRAFT_SERVER, "Packet")
        "PacketPlayOutTitle".associate(NET_MINECRAFT_SERVER, "PacketPlayOutTitle")
        "PacketPlayOutChat".associate(NET_MINECRAFT_SERVER, "PacketPlayOutChat")
        "PacketPlayOutPlayerListHeaderFooter".associate(NET_MINECRAFT_SERVER, "PacketPlayOutPlayerListHeaderFooter")
        "EnumTitleAction".associate(NET_MINECRAFT_SERVER, "EnumTitleAction")
        "PacketPlayOutScoreboardObjective".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardObjective")
        "PacketPlayOutScoreboardDisplayObjective".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardDisplayObjective")
        "PacketPlayOutScoreboardScore".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardScore")
        "PacketPlayOutScoreboardTeam".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardTeam")
        "EnumScoreboardHealthDisplay".associate(NET_MINECRAFT_SERVER, "EnumScoreboardHealthDisplay")
        "EnumScoreboardAction".associate(NET_MINECRAFT_SERVER, "EnumScoreboardAction")
    }
}

/**
 * Provides classes for Minecraft 1.8.3 - 1.9.X
 */
object Provider183 : NMSClassProvider() {
    init {
        "ChatComponentText".associate(NET_MINECRAFT_SERVER, "ChatComponentText")
        "IChatBaseComponent".associate(NET_MINECRAFT_SERVER, "IChatBaseComponent")
        "CraftPlayer".associate(ORG_BUKKIT_CRAFTBUKKIT, "entity.CraftPlayer")
        "EntityPlayer".associate(NET_MINECRAFT_SERVER, "EntityPlayer")
        "PlayerConnection".associate(NET_MINECRAFT_SERVER, "PlayerConnection")
        "NetworkManager".associate(NET_MINECRAFT_SERVER, "NetworkManager")
        "Packet".associate(NET_MINECRAFT_SERVER, "Packet")
        "PacketPlayOutTitle".associate(NET_MINECRAFT_SERVER, "PacketPlayOutTitle")
        "PacketPlayOutChat".associate(NET_MINECRAFT_SERVER, "PacketPlayOutChat")
        "PacketPlayOutPlayerListHeaderFooter".associate(NET_MINECRAFT_SERVER, "PacketPlayOutPlayerListHeaderFooter")
        "EnumTitleAction".associate(NET_MINECRAFT_SERVER, "PacketPlayOutTitle", "EnumTitleAction")
        "PacketPlayOutScoreboardObjective".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardObjective")
        "PacketPlayOutScoreboardDisplayObjective".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardDisplayObjective")
        "PacketPlayOutScoreboardScore".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardScore")
        "PacketPlayOutScoreboardTeam".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardTeam")
        "EnumScoreboardHealthDisplay".associate(NET_MINECRAFT_SERVER, "IScoreboardCriteria", "EnumScoreboardHealthDisplay")
        "EnumScoreboardAction".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardScore", "EnumScoreboardAction")
    }
}

/**
 * Provides classes for Minecraft 1.10 - 1.11
 */
object Provider110 : NMSClassProvider() {
    init {
        "ChatComponentText".associate(NET_MINECRAFT_SERVER, "ChatComponentText")
        "IChatBaseComponent".associate(NET_MINECRAFT_SERVER, "IChatBaseComponent")
        "CraftPlayer".associate(ORG_BUKKIT_CRAFTBUKKIT, "entity.CraftPlayer")
        "EntityPlayer".associate(NET_MINECRAFT_SERVER, "EntityPlayer")
        "PlayerConnection".associate(NET_MINECRAFT_SERVER, "PlayerConnection")
        "NetworkManager".associate(NET_MINECRAFT_SERVER, "NetworkManager")
        "Packet".associate(NET_MINECRAFT_SERVER, "Packet")
        "PacketPlayOutTitle".associate(NET_MINECRAFT_SERVER, "PacketPlayOutTitle")
        "PacketPlayOutChat".associate(NET_MINECRAFT_SERVER, "PacketPlayOutChat")
        "PacketPlayOutPlayerListHeaderFooter".associate(NET_MINECRAFT_SERVER, "PacketPlayOutPlayerListHeaderFooter")
        "EnumTitleAction".associate(NET_MINECRAFT_SERVER, "PacketPlayOutTitle", "EnumTitleAction")
        "PacketPlayOutScoreboardObjective".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardObjective")
        "PacketPlayOutScoreboardDisplayObjective".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardDisplayObjective")
        "PacketPlayOutScoreboardScore".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardScore")
        "PacketPlayOutScoreboardTeam".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardTeam")
        "EnumScoreboardHealthDisplay".associate(NET_MINECRAFT_SERVER, "IScoreboardCriteria", "EnumScoreboardHealthDisplay")
        "EnumScoreboardAction".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardScore", "EnumScoreboardAction")
    }
}

/**
 * Provides classes for Minecraft 1.12
 */
object Provider112 : NMSClassProvider() {
    init {
        "ChatComponentText".associate(NET_MINECRAFT_SERVER, "ChatComponentText")
        "IChatBaseComponent".associate(NET_MINECRAFT_SERVER, "IChatBaseComponent")
        "CraftPlayer".associate(ORG_BUKKIT_CRAFTBUKKIT, "entity.CraftPlayer")
        "EntityPlayer".associate(NET_MINECRAFT_SERVER, "EntityPlayer")
        "PlayerConnection".associate(NET_MINECRAFT_SERVER, "PlayerConnection")
        "NetworkManager".associate(NET_MINECRAFT_SERVER, "NetworkManager")
        "Packet".associate(NET_MINECRAFT_SERVER, "Packet")
        "PacketPlayOutTitle".associate(NET_MINECRAFT_SERVER, "PacketPlayOutTitle")
        "PacketPlayOutChat".associate(NET_MINECRAFT_SERVER, "PacketPlayOutChat")
        "PacketPlayOutPlayerListHeaderFooter".associate(NET_MINECRAFT_SERVER, "PacketPlayOutPlayerListHeaderFooter")
        "EnumTitleAction".associate(NET_MINECRAFT_SERVER, "PacketPlayOutTitle", "EnumTitleAction")
        "PacketPlayOutScoreboardObjective".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardObjective")
        "PacketPlayOutScoreboardDisplayObjective".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardDisplayObjective")
        "PacketPlayOutScoreboardScore".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardScore")
        "PacketPlayOutScoreboardTeam".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardTeam")
        "EnumScoreboardHealthDisplay".associate(NET_MINECRAFT_SERVER, "IScoreboardCriteria", "EnumScoreboardHealthDisplay")
        "EnumScoreboardAction".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardScore", "EnumScoreboardAction")
    }
}

/**
 * Provides classes for Minecraft 1.13 - 1.15
 */
object Provider113 : NMSClassProvider() {
    init {
        "ChatComponentText".associate(NET_MINECRAFT_SERVER, "ChatComponentText")
        "IChatBaseComponent".associate(NET_MINECRAFT_SERVER, "IChatBaseComponent")
        "CraftPlayer".associate(ORG_BUKKIT_CRAFTBUKKIT, "entity.CraftPlayer")
        "EntityPlayer".associate(NET_MINECRAFT_SERVER, "EntityPlayer")
        "PlayerConnection".associate(NET_MINECRAFT_SERVER, "PlayerConnection")
        "NetworkManager".associate(NET_MINECRAFT_SERVER, "NetworkManager")
        "Packet".associate(NET_MINECRAFT_SERVER, "Packet")
        "PacketPlayOutTitle".associate(NET_MINECRAFT_SERVER, "PacketPlayOutTitle")
        "PacketPlayOutChat".associate(NET_MINECRAFT_SERVER, "PacketPlayOutChat")
        "PacketPlayOutPlayerListHeaderFooter".associate(NET_MINECRAFT_SERVER, "PacketPlayOutPlayerListHeaderFooter")
        "EnumTitleAction".associate(NET_MINECRAFT_SERVER, "PacketPlayOutTitle", "EnumTitleAction")
        "PacketPlayOutScoreboardObjective".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardObjective")
        "PacketPlayOutScoreboardDisplayObjective".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardDisplayObjective")
        "PacketPlayOutScoreboardScore".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardScore")
        "PacketPlayOutScoreboardTeam".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardTeam")
        "EnumScoreboardHealthDisplay".associate(NET_MINECRAFT_SERVER, "IScoreboardCriteria", "EnumScoreboardHealthDisplay")
        "EnumScoreboardAction".associate(NET_MINECRAFT_SERVER, "ScoreboardServer", "Action")
    }
}

/**
 * Provides classes for Minecraft 1.16 <=
 */
object Provider116 : NMSClassProvider() {
    init {
        "ChatComponentText".associate(NET_MINECRAFT_SERVER, "ChatComponentText")
        "IChatBaseComponent".associate(NET_MINECRAFT_SERVER, "IChatBaseComponent")
        "ChatSerializer".associate(NET_MINECRAFT_SERVER, "IChatBaseComponent\$ChatSerializer")
        "CraftPlayer".associate(ORG_BUKKIT_CRAFTBUKKIT, "entity.CraftPlayer")
        "EntityPlayer".associate(NET_MINECRAFT_SERVER, "EntityPlayer")
        "PlayerConnection".associate(NET_MINECRAFT_SERVER, "PlayerConnection")
        "NetworkManager".associate(NET_MINECRAFT_SERVER, "NetworkManager")
        "Packet".associate(NET_MINECRAFT_SERVER, "Packet")
        "PacketPlayOutTitle".associate(NET_MINECRAFT_SERVER, "PacketPlayOutTitle")
        "PacketPlayOutChat".associate(NET_MINECRAFT_SERVER, "PacketPlayOutChat")
        "PacketPlayOutPlayerListHeaderFooter".associate(NET_MINECRAFT_SERVER, "PacketPlayOutPlayerListHeaderFooter")
        "EnumTitleAction".associate(NET_MINECRAFT_SERVER, "PacketPlayOutTitle", "EnumTitleAction")
        "PacketPlayOutScoreboardObjective".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardObjective")
        "PacketPlayOutScoreboardDisplayObjective".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardDisplayObjective")
        "PacketPlayOutScoreboardScore".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardScore")
        "PacketPlayOutScoreboardTeam".associate(NET_MINECRAFT_SERVER, "PacketPlayOutScoreboardTeam")
        "EnumScoreboardHealthDisplay".associate(NET_MINECRAFT_SERVER, "IScoreboardCriteria", "EnumScoreboardHealthDisplay")
        "EnumScoreboardAction".associate(NET_MINECRAFT_SERVER, "ScoreboardServer", "Action")
    }
}
