package dev.tarkan.titlemanager.bukkit.storage

import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.storage.schema.PlayerInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class SqlitePlayerStorage(private val plugin: TitleManagerPlugin) : PlayerStorage {
    private val databaseFile: File = File(plugin.dataFolder, "playerinfo.sqlite")

    private val connection: Connection
    private val connectionLock: Lock = ReentrantLock()

    private val cache: MutableMap<UUID, PlayerInfo> = mutableMapOf()

    init {
        Class.forName("org.sqlite.JDBC")

        ensureDatabaseFile()
        connection = DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}")
        createDatabaseSchema()
    }

    override fun get(player: Player) = get(player.uniqueId)

    override fun get(uuid: UUID): PlayerInfo {
        return cache[uuid] ?: throw IllegalArgumentException("UUID '$uuid' doesn't exist in cache")
    }

    override suspend fun load(uuid: UUID): PlayerInfo {
        val playerInfo = getOrCreatePlayerInfo(uuid)

        cache[uuid] = playerInfo

        return playerInfo
    }

    override fun unload(uuid: UUID) {
        cache.remove(uuid)
    }

    override suspend fun setSidebarEnabled(uuid: UUID, enabled: Boolean) {
        cache[uuid] = get(uuid).copy(isSidebarEnabled = enabled)

        connectionLock.withLock {
            connection.prepareStatement("UPDATE playerinfo SET is_sidebar_enabled = ? WHERE uuid = ?;").use { statement ->
                statement.setBoolean(1, enabled)
                statement.setString(2, uuid.toString())

                statement.executeUpdate()
            }
        }

    }

    override suspend fun setPlayerListEnabled(uuid: UUID, enabled: Boolean) {
        cache[uuid] = get(uuid).copy(isPlayerListEnabled = enabled)

        connectionLock.withLock {
            connection.prepareStatement("UPDATE playerinfo SET is_player_list_enabled = ? WHERE uuid = ?;").use { statement ->
                statement.setBoolean(1, enabled)
                statement.setString(2, uuid.toString())

                statement.executeUpdate()
            }
        }
    }

    override suspend fun setWelcomeTitleEnabled(uuid: UUID, enabled: Boolean) {
        cache[uuid] = get(uuid).copy(isWelcomeTitleEnabled = enabled)

        connectionLock.withLock {
            connection.prepareStatement("UPDATE playerinfo SET is_welcome_title_enabled = ? WHERE uuid = ?;").use { statement ->
                statement.setBoolean(1, enabled)
                statement.setString(2, uuid.toString())

                statement.executeUpdate()
            }
        }
    }

    override suspend fun setWelcomeActionbarEnabled(uuid: UUID, enabled: Boolean) {
        cache[uuid] = get(uuid).copy(isWelcomeActionbarEnabled = enabled)

        connectionLock.withLock {
            connection.prepareStatement("UPDATE playerinfo SET is_welcome_actionbar_enabled = ? WHERE uuid = ?;").use { statement ->
                statement.setBoolean(1, enabled)
                statement.setString(2, uuid.toString())

                statement.executeUpdate()
            }
        }
    }

    override fun close() {
        connection.close()
    }

    private suspend fun getOrCreatePlayerInfo(uuid: UUID, dispatcher: CoroutineDispatcher = Dispatchers.IO): PlayerInfo = withContext(dispatcher) {
        connectionLock.withLock {
            var playerInfo = getPlayerInfo(uuid)

            if (playerInfo == null) {
                playerInfo = PlayerInfo(uuid = uuid)

                insertPlayerInfo(playerInfo)
            }

            playerInfo
        }
    }

    private fun getPlayerInfo(uuid: UUID): PlayerInfo? {
        return connection.prepareStatement("SELECT * FROM playerinfo WHERE uuid = ?;").use { statement ->
            statement.setString(1, uuid.toString())

            statement.executeQuery().use ResultSet@ { resultSet ->
                if (!resultSet.next()) {
                    return@ResultSet null
                }

                return@ResultSet PlayerInfo(
                    uuid = uuid,
                    isSidebarEnabled = resultSet.getBoolean("is_sidebar_enabled"),
                    isPlayerListEnabled = resultSet.getBoolean("is_player_list_enabled"),
                    isWelcomeTitleEnabled = resultSet.getBoolean("is_welcome_title_enabled"),
                    isWelcomeActionbarEnabled = resultSet.getBoolean("is_welcome_actionbar_enabled")
                )
            }
        }
    }

    private fun insertPlayerInfo(playerInfo: PlayerInfo) {
        connection.prepareStatement("INSERT INTO playerinfo (uuid, is_sidebar_enabled, is_player_list_enabled, is_welcome_title_enabled, is_welcome_actionbar_enabled) VALUES (?, ?, ?, ?, ?);").use { statement ->
            statement.setString(1, playerInfo.uuid.toString())
            statement.setBoolean(2, playerInfo.isSidebarEnabled)
            statement.setBoolean(3, playerInfo.isPlayerListEnabled)
            statement.setBoolean(4, playerInfo.isWelcomeTitleEnabled)
            statement.setBoolean(5, playerInfo.isWelcomeActionbarEnabled)

            statement.executeUpdate()
        }
    }

    private fun updatePlayerInfo(playerInfo: PlayerInfo) {
        connection.prepareStatement("UPDATE playerinfo SET is_sidebar_enabled = ?, is_player_list_enabled = ?, is_welcome_title_enabled = ?, is_welcome_actionbar_enabled = ? WHERE uuid = ?;").use { statement ->
            statement.setBoolean(1, playerInfo.isSidebarEnabled)
            statement.setBoolean(2, playerInfo.isPlayerListEnabled)
            statement.setBoolean(3, playerInfo.isWelcomeTitleEnabled)
            statement.setBoolean(4, playerInfo.isWelcomeActionbarEnabled)

            statement.setString(5, playerInfo.uuid.toString())
            statement.executeUpdate()
        }
    }

    private fun ensureDatabaseFile() {
        val parentFile = databaseFile.parentFile

        if (parentFile != null && !parentFile.exists() && !parentFile.mkdirs()) {
            error("Unable to create database directory '${parentFile.absolutePath}'")
        }

        if (databaseFile.exists()) {
            if (!databaseFile.isFile) {
                error("Database path '${databaseFile.absolutePath}' is not a file")
            }

            return
        }

        if (!databaseFile.createNewFile()) {
            error("Unable to create database file '${databaseFile.absolutePath}'")
        }
    }

    private fun createDatabaseSchema() {
        val createStatement = requireNotNull(plugin.getResource("Schema/SQLite/create_database.sql")) {
            "Missing SQLite schema resource"
        }.reader().use { it.readText() }

        connectionLock.withLock {
            connection.createStatement().use { statement ->
                statement.executeUpdate(createStatement)
            }

            migratePlayerInfoSchema()
        }
    }

    private fun migratePlayerInfoSchema() {
        var columns = playerInfoColumns()

        if ("scoreboard_toggled" in columns && "is_sidebar_enabled" !in columns) {
            addBooleanColumn("is_sidebar_enabled")
            connection.createStatement().use { statement ->
                statement.executeUpdate("UPDATE playerinfo SET is_sidebar_enabled = scoreboard_toggled;")
            }
            columns = playerInfoColumns()
        }

        for (column in listOf("is_player_list_enabled", "is_welcome_title_enabled", "is_welcome_actionbar_enabled")) {
            if (column !in columns) {
                addBooleanColumn(column)
            }
        }
    }

    private fun playerInfoColumns(): Set<String> {
        return connection.createStatement().use { statement ->
            statement.executeQuery("PRAGMA table_info(playerinfo);").use { resultSet ->
                buildSet {
                    while (resultSet.next()) {
                        add(resultSet.getString("name"))
                    }
                }
            }
        }
    }

    private fun addBooleanColumn(columnName: String) {
        connection.createStatement().use { statement ->
            statement.executeUpdate("ALTER TABLE playerinfo ADD COLUMN $columnName INTEGER NOT NULL DEFAULT 1;")
        }
    }
}