package io.puharesource.mc.titlemanager.internal.services.update

import io.puharesource.mc.titlemanager.TitleManagerPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection

class UpdateServiceSpigot @Inject constructor(val plugin: TitleManagerPlugin) : UpdateService {
    private val spigotAPIUrl = URL("https://api.spigotmc.org/legacy/update.php?resource=1049")

    override val currentVersion: String
        get() = plugin.description.version

    override var latestVersion: String = plugin.description.version
        private set

    override val isUpdateAvailable: Boolean
        get() = currentVersion != latestVersion

    override suspend fun updateLatestVersionCache() {
        withContext(Dispatchers.IO) {
            val connection = spigotAPIUrl.openConnection() as HttpsURLConnection
            connection.requestMethod = "GET"

            latestVersion = connection.inputStream.use { it.bufferedReader().readText() }

            connection.disconnect()
        }
    }
}
