package dev.tarkan.titlemanager.bukkit.update

import java.net.URI

fun interface UpdateClient {
    fun latestVersion(currentVersion: String): String
}

class GitHubReleaseUpdateClient(
    private val repository: String = "Puharesource/TitleManager",
    private val snapshotVersion: String = "3.0.0-SNAPSHOT"
) : UpdateClient {
    override fun latestVersion(currentVersion: String): String {
        val current = SemanticVersion.parse(currentVersion) ?: return ""
        val releasesJson = URI("https://api.github.com/repos/$repository/releases?per_page=100")
            .toURL()
            .openStream()
            .use { stream -> stream.bufferedReader().readText() }

        return RELEASE_PATTERN.findAll(releasesJson)
            .mapNotNull { match -> match.groups["tag"]?.value }
            .map { tag -> if (tag.startsWith("snapshot-")) "$snapshotVersion+${tag.removePrefix("snapshot-")}" else tag }
            .mapNotNull { tag -> SemanticVersion.parse(tag)?.let { version -> tag to version } }
            .filter { (_, version) -> version.isEligibleUpdateFor(current) }
            .maxByOrNull { (_, version) -> version }
            ?.first
            .orEmpty()
    }

    private companion object {
        private val RELEASE_PATTERN = Regex(""""tag_name"\s*:\s*"(?<tag>[^"]+)"""")
    }
}
