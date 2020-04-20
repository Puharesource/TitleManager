package io.puharesource.mc.titlemanager.internal.services.storage

import io.puharesource.mc.titlemanager.TitleManagerPlugin
import org.bukkit.entity.Player
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

class PlayerInfoServiceSqlite @Inject constructor(val plugin: TitleManagerPlugin) : PlayerInfoService {
    private val databaseFile: File = File(plugin.dataFolder, "playerinfo.sqlite")

    private val connection: Connection
    private val connectionLock: Lock = ReentrantLock()

    private val Boolean.intValue: Int get() = if (this) 1 else 0
    private val Int.booleanValue: Boolean get() = this != 0

    init {
        Class.forName("org.sqlite.JDBC")

        connection = DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}")
        createDatabaseFile()
    }

    override fun isScoreboardEnabled(player: Player): Boolean {
        var isToggled = true

        connectionLock.withLock {
            connection.prepareStatement("SELECT * FROM playerinfo WHERE uuid = ?;").use { statement ->
                statement.setString(1, player.uniqueId.toString())

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        isToggled = resultSet.getInt("scoreboard_toggled").booleanValue
                    }
                }
            }
        }

        return isToggled
    }

    override fun showScoreboard(player: Player, show: Boolean) {
        connectionLock.withLock {
            val exists = playerExists(player)

            if (exists) {
                updatePlayer(player, show)
            } else {
                insertPlayer(player, show)
            }
        }
    }

    private fun createDatabaseFile() {
        val createStatement = plugin.getResource("playerinfo.sql")!!.reader().readText()

        connectionLock.withLock {
            if (!databaseFile.exists()) {
                databaseFile.mkdirs()
                databaseFile.createNewFile()
            }

            val statement = connection.createStatement()
            statement.executeUpdate(createStatement)
        }
    }

    private fun playerExists(player: Player): Boolean {
        var exists = false

        connection.prepareStatement("SELECT * FROM playerinfo WHERE uuid = ?;").use { statement ->
            statement.setString(1, player.uniqueId.toString())

            statement.executeQuery().use { resultSet ->
                exists = resultSet.next()
            }
        }

        return exists
    }

    private fun insertPlayer(player: Player, show: Boolean) {
        connection.prepareStatement("INSERT INTO playerinfo (uuid, scoreboard_toggled) VALUES (?, ?);").use { statement ->
            statement.setString(1, player.uniqueId.toString())
            statement.setInt(2, show.intValue)

            statement.executeUpdate()
        }
    }

    private fun updatePlayer(player: Player, show: Boolean) {
        connection.prepareStatement("UPDATE playerinfo SET scoreboard_toggled = ? WHERE uuid = ?;").use { statement ->
            statement.setInt(1, show.intValue)
            statement.setString(2, player.uniqueId.toString())

            statement.executeUpdate()
        }
    }
}
