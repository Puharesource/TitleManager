package io.puharesource.mc.titlemanager.playerinfo

import org.bukkit.entity.Player
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class PlayerInfoDB(file: File, createStatement: String?) {
    private val connection : Connection
    private val connectionLock : Lock = ReentrantLock()

    init {
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:${file.absolutePath}")

        connectionLock.withLock {
            if (!file.exists()) {
                file.mkdirs()
                file.createNewFile()
            }

            if (createStatement != null) {
                val statement = connection.createStatement()
                statement.executeUpdate(createStatement)
            }
        }
    }

    fun isScoreboardToggled(player: Player): Boolean {
        var isToggled = true

        connectionLock.withLock {
            connection.prepareStatement("SELECT * FROM playerinfo WHERE uuid = ?;").use { statement ->
                statement.setString(1, player.uniqueId.toString())

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        isToggled = resultSet.getInt("scoreboard_toggled") != 0
                    }
                }
            }
        }

        return isToggled
    }

    fun setScoreboardToggled(player: Player, toggled: Boolean) {
        connectionLock.withLock {
            val uuidString = player.uniqueId.toString()
            val toggledValue = if (toggled) 1 else 0
            var exists = false

            connection.prepareStatement("SELECT * FROM playerinfo WHERE uuid = ?;").use { statement ->
                statement.setString(1, player.uniqueId.toString())

                statement.executeQuery().use { resultSet ->
                    exists = resultSet.next()
                }
            }

            if (exists) {
                connection.prepareStatement("UPDATE playerinfo SET scoreboard_toggled = ? WHERE uuid = ?;").use { statement ->
                    statement.setInt(1, toggledValue)
                    statement.setString(2, uuidString)
                }
            } else {
                connection.prepareStatement("INSERT INTO playerinfo (uuid, scoreboard_toggled) VALUES (?, ?);").use { statement ->
                    statement.setString(1, uuidString)
                    statement.setInt(2, toggledValue)
                }
            }
        }
    }
}