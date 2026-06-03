package dev.tarkan.titlemanager.bukkit.animation

import dev.tarkan.titlemanager.parser.IntermediaryParser
import dev.tarkan.titlemanager.parser.animation.AnimationParser
import dev.tarkan.titlemanager.parser.animation.serialization.StringAnimationDataSerializer
import dev.tarkan.titlemanager.parser.placeholder.animation.AnimationPlaceholderRegistry
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText

fun <TContext> AnimationPlaceholderRegistry.Builder<TContext>.addAnimationFilePlaceholders(
    animationFiles: Iterable<Path>,
    intermediaryParser: IntermediaryParser,
    animationParser: () -> AnimationParser<TContext>
): AnimationPlaceholderRegistry.Builder<TContext> {
    for (animationFile in animationFiles) {
        addSimple(animationFile.nameWithoutExtension, dataSerializer = StringAnimationDataSerializer) {
            val parsed = intermediaryParser.parseText(animationFile.readText())

            animationParser().parseAnimation(parsed)
        }
    }

    return this
}
