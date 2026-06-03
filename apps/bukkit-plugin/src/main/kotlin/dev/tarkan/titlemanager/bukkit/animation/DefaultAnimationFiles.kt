package dev.tarkan.titlemanager.bukkit.animation

import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.outputStream

internal object DefaultAnimationFiles {
    const val ANIMATIONS_DIRECTORY_NAME = "animations"
    private const val DEFAULT_ANIMATION_RESOURCE_DIRECTORY = "DefaultAnimations"

    val FILE_NAMES = listOf(
        "left-to-right.txt",
        "right-to-left.txt"
    )

    fun copyMissingTo(dataFolder: Path, openResource: (String) -> InputStream?): Path {
        val animationsFolder = dataFolder.resolve(ANIMATIONS_DIRECTORY_NAME)
        animationsFolder.createDirectories()

        for (fileName in FILE_NAMES) {
            val target = animationsFolder.resolve(fileName)
            if (target.exists() && target.fileSize() > 0L) {
                continue
            }

            val resourcePath = resourcePath(fileName)
            openResource(resourcePath)?.use { defaultAnimationStream ->
                target.outputStream().use(defaultAnimationStream::transferTo)
            } ?: error("Missing default animation resource: $resourcePath")
        }

        return animationsFolder
    }

    private fun resourcePath(fileName: String): String = "$DEFAULT_ANIMATION_RESOURCE_DIRECTORY/$fileName"
}
