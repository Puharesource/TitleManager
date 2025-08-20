package studio.minekarta.titlemanagerreborn.internal.services.update

interface UpdateService {
    val currentVersion: String
    val latestVersion: String
    val isUpdateAvailable: Boolean

    suspend fun updateLatestVersionCache()
}
