package dev.tarkan.titlemanager.bukkit.runtime

object RuntimeTextConstants {
    const val SCOREBOARD_DEFAULT_TITLE = "TitleManager"
    const val SCOREBOARD_OBJECTIVE_CRITERIA = "dummy"
    const val LEGACY_SCOREBOARD_TITLE_LIMIT = 32
    const val LEGACY_SCOREBOARD_TEAM_TEXT_LIMIT = 16

    val SCOREBOARD_ENTRIES: Array<String> = arrayOf(
        "§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7",
        "§8", "§9", "§a", "§b", "§c", "§d", "§e", "§f"
    )

    fun scoreboardEntry(index: Int): String = SCOREBOARD_ENTRIES[index]

    fun legacyScoreboardEntry(index: Int): String = SCOREBOARD_ENTRIES[index % SCOREBOARD_ENTRIES.size]

    fun sidebarScore(index: Int): Int = index - 1
}

object RuntimeCapabilityDetail {
    const val ADVENTURE_API = "Adventure API"
    const val BUKKIT_SCOREBOARD_API = "Bukkit scoreboard API"
    const val BUKKIT_SCOREBOARD_STRING_API = "Bukkit scoreboard string API"
    const val SPIGOT_TITLE_API = "Spigot title API"
    const val SPIGOT_TITLE_API_WITHOUT_TIMING = "Spigot title API without timing control"
    const val SPIGOT_CHAT_MESSAGE_TYPE_API = "Spigot ChatMessageType API"
    const val DIRECT_PLAYER_LIST_PACKET = "direct NMS PacketPlayOutPlayerListHeaderFooter"
    const val DIRECT_TITLE_PACKET = "direct NMS PacketPlayOutTitle"
    const val DIRECT_ACTIONBAR_PACKET = "direct NMS PacketPlayOutChat position 2"
    const val LEGACY_PLAYER_LIST_REQUIRES_NMS = "player-list header/footer requires Bukkit API 1.17+ or a direct NMS module"
    const val LEGACY_ACTIONBAR_REQUIRES_NMS = "actionbar requires Spigot 1.12+ or a direct NMS module"
}
