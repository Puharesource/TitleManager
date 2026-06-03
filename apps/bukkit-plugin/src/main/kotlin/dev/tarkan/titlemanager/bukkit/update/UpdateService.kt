package dev.tarkan.titlemanager.bukkit.update

import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.concurrency.CoroutineScopeManager
import dev.tarkan.titlemanager.bukkit.configuration.AdvancedConfiguration
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference

class UpdateService(
    private val plugin: TitleManagerPlugin,
    private val advancedConfiguration: AdvancedConfiguration,
    private val coroutineScopeManager: CoroutineScopeManager,
    private val updateClient: UpdateClient
) {
    private val latestVersionReference = AtomicReference<String?>(null)

    val currentVersion: String
        get() = plugin.pluginVersion

    val latestVersion: String?
        get() = latestVersionReference.get()

    val enabled: Boolean
        get() = advancedConfiguration.checkForUpdates

    val isUpdateAvailable: Boolean
        get() = latestVersion?.let { isNewerVersion(it, currentVersion) } ?: false

    fun start() {
        if (!enabled) {
            return
        }

        coroutineScopeManager.scope.launch {
            refresh()
        }
    }

    fun refresh() {
        if (!enabled) {
            return
        }

        try {
            latestVersionReference.set(updateClient.latestVersion(currentVersion).takeIf { it.isNotBlank() })
        } catch (exception: IOException) {
            plugin.logger.warning("Failed to check for TitleManager updates: ${exception.message}")
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestVersion = SemanticVersion.parse(latest) ?: return false
        val currentVersion = SemanticVersion.parse(current) ?: return false

        return latestVersion.isEligibleUpdateFor(currentVersion) && latestVersion > currentVersion
    }
}

internal class SemanticVersion private constructor(
    private val major: Long,
    private val minor: Long,
    private val patch: Long,
    private val preRelease: List<String>,
    private val buildMetadata: String?
) : Comparable<SemanticVersion> {
    private val channel: ReleaseChannel = when (preRelease.firstOrNull()?.lowercase()) {
        null -> ReleaseChannel.STABLE
        "beta" -> ReleaseChannel.BETA
        "alpha" -> ReleaseChannel.ALPHA
        "snapshot" -> ReleaseChannel.SNAPSHOT
        else -> ReleaseChannel.OTHER
    }

    fun isEligibleUpdateFor(current: SemanticVersion): Boolean = channel.canUpdate(current.channel)
    override fun compareTo(other: SemanticVersion): Int {
        compareValues(major, other.major).takeIf { it != 0 }?.let { return it }
        compareValues(minor, other.minor).takeIf { it != 0 }?.let { return it }
        compareValues(patch, other.patch).takeIf { it != 0 }?.let { return it }

        if (preRelease.isEmpty() && other.preRelease.isNotEmpty()) {
            return 1
        }
        if (preRelease.isNotEmpty() && other.preRelease.isEmpty()) {
            return -1
        }

        val maxIdentifiers = maxOf(preRelease.size, other.preRelease.size)
        for (index in 0 until maxIdentifiers) {
            val left = preRelease.getOrNull(index) ?: return -1
            val right = other.preRelease.getOrNull(index) ?: return 1
            val comparison = comparePreReleaseIdentifier(left, right)

            if (comparison != 0) {
                return comparison
            }
        }

        if (channel == ReleaseChannel.SNAPSHOT && other.channel == ReleaseChannel.SNAPSHOT && buildMetadata != other.buildMetadata) {
            return when {
                buildMetadata == null -> -1
                other.buildMetadata == null -> 1
                else -> buildMetadata.compareTo(other.buildMetadata)
            }
        }

        return 0
    }

    companion object {
        fun parse(value: String): SemanticVersion? {
            val normalized = value.trim()
                .removePrefix("v")
                .removePrefix("V")
            val version = normalized.substringBefore('+')
            val buildMetadata = normalized.substringAfter('+', missingDelimiterValue = "").takeIf { it.isNotEmpty() }
            val versionAndPreRelease = version.split('-', limit = 2)
            val coreIdentifiers = versionAndPreRelease[0].split('.')

            if (coreIdentifiers.size != 3) {
                return null
            }

            val major = coreIdentifiers[0].toSemanticVersionNumberOrNull() ?: return null
            val minor = coreIdentifiers[1].toSemanticVersionNumberOrNull() ?: return null
            val patch = coreIdentifiers[2].toSemanticVersionNumberOrNull() ?: return null
            val preRelease = if (versionAndPreRelease.size == 2) {
                val identifiers = versionAndPreRelease[1].split('.')

                if (identifiers.isEmpty() || identifiers.any { !it.isValidPreReleaseIdentifier() }) {
                    return null
                }

                identifiers
            } else {
                emptyList()
            }

            return SemanticVersion(major, minor, patch, preRelease, buildMetadata)
        }

        private fun comparePreReleaseIdentifier(left: String, right: String): Int {
            val leftNumber = left.toSemanticVersionNumberOrNull()
            val rightNumber = right.toSemanticVersionNumberOrNull()

            if (leftNumber != null && rightNumber != null) {
                return compareValues(leftNumber, rightNumber)
            }
            if (leftNumber != null) {
                return -1
            }
            if (rightNumber != null) {
                return 1
            }

            return left.compareTo(right)
        }
    }

private enum class ReleaseChannel {
    STABLE,
    BETA,
    ALPHA,
    SNAPSHOT,
    OTHER;

    fun canUpdate(current: ReleaseChannel): Boolean = when (current) {
        STABLE -> this == STABLE
        BETA -> this == STABLE || this == BETA
        ALPHA -> this == STABLE || this == BETA || this == ALPHA
        SNAPSHOT -> this == STABLE || this == BETA || this == ALPHA || this == SNAPSHOT
        OTHER -> false
    }
}
}

private fun String.toSemanticVersionNumberOrNull(): Long? {
    if (isEmpty() || length > 1 && this[0] == '0') {
        return null
    }
    if (!all { it.isDigit() }) {
        return null
    }

    return toLongOrNull()
}

private fun String.isValidPreReleaseIdentifier(): Boolean {
    if (isEmpty() || any { !it.isLetterOrDigit() && it != '-' }) {
        return false
    }

    return toLongOrNull() == null || toSemanticVersionNumberOrNull() != null
}
