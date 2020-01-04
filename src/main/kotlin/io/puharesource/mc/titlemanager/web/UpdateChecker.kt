package io.puharesource.mc.titlemanager.web

import io.puharesource.mc.titlemanager.debug
import io.puharesource.mc.titlemanager.info
import io.puharesource.mc.titlemanager.pluginInstance
import io.puharesource.mc.titlemanager.scheduling.scheduleAsyncTimer
import org.bukkit.scheduler.BukkitTask
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object UpdateChecker {
    private var updateTask : BukkitTask? = null

    private var latestVersion : String? = null

    private val spigotAPIUrl = URL("https://api.spigotmc.org/legacy/update.php?resource=1049")

    private val isRunning: Boolean
        get() = updateTask != null

    fun start() {
        updateTask = scheduleAsyncTimer(period = 20 * 60 * 10) {
            debug("Searching for updates...")

            try {
                latestVersion = retrieveVersion()
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
        if (isRunning) {
            updateTask!!.cancel()
            latestVersion = null
        }
    }

    fun getCurrentVersion() : String = pluginInstance.description.version

    fun getLatestVersion() = latestVersion

    fun isUpdateAvailable() = latestVersion != null && !getCurrentVersion().equals(latestVersion, ignoreCase = true)

    private fun retrieveVersion(): String {
        val connection = spigotAPIUrl.openConnection() as HttpsURLConnection
        connection.requestMethod = "GET"

        val version = connection.inputStream.use { it.bufferedReader().readText() }

        connection.disconnect()

        return version
    }
}