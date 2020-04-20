package io.puharesource.mc.titlemanager.internal.services.update

interface UpdateService {
    val currentVersion: String
    val latestVersion: String
    val isUpdateAvailable: Boolean

    suspend fun updateLatestVersionCache()
}
