package io.puharesource.mc.titlemanager.web

import io.puharesource.mc.titlemanager.debug
import io.puharesource.mc.titlemanager.info
import io.puharesource.mc.titlemanager.pluginInstance
import org.bukkit.scheduler.BukkitTask
import scheduleAsyncTimer
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {
    private var updateTask : BukkitTask? = null

    private var latestVersion : String? = null

    private val spigotAPIUrl = URL("http://www.spigotmc.org/api/general.php")
    private val apiKey = "key=98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4&resource=1049"

    fun start() {
        updateTask = scheduleAsyncTimer(period = 20 * 60 * 10) {
            debug("Searching for updates...")

            try {
                val connection = spigotAPIUrl.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.requestMethod = "POST"
                connection.outputStream.write(apiKey.toByteArray())

                val reader = BufferedReader(InputStreamReader(connection.inputStream))

                latestVersion = reader.readLine()

                reader.close()
                connection.disconnect()
            } catch (e: IOException) {
                debug("Failed to get information for update check.")
            }

            if (isUpdateAvailable()) {
                info("An update was found!")
            } else {
                debug("No update was found.")
            }
        }
    }

    fun stop() {
        if (isRunning()) {
            updateTask!!.cancel()
            latestVersion = null
        }
    }

    fun isRunning() = updateTask != null

    fun getCurrentVersion() : String = pluginInstance.description.version

    fun getLatestVersion() = latestVersion

    fun isUpdateAvailable() : Boolean {
        return latestVersion != null && !getCurrentVersion().equals(latestVersion, ignoreCase = true)
    }
}