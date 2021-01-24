package io.puharesource.mc.titlemanager.internal.services.animation

import com.google.common.collect.ImmutableMap
import io.puharesource.mc.titlemanager.TitleManagerPlugin
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import io.puharesource.mc.titlemanager.internal.extensions.color
import io.puharesource.mc.titlemanager.internal.model.animation.StandardAnimationFrame
import io.puharesource.mc.titlemanager.internal.services.placeholder.PlaceholderService
import java.io.File
import java.util.concurrent.ConcurrentSkipListMap
import javax.inject.Inject

class AnimationsServiceFile @Inject constructor(private val plugin: TitleManagerPlugin, private val scriptService: ScriptService, private val placeholderService: PlaceholderService) : AnimationsService {
    private val animationsFolder = File(plugin.dataFolder, "animations")
    private val textAnimationFramePattern = "^\\[([-]?\\d+);([-]?\\d+);([-]?\\d+)](.+)$".toRegex()
    private val animationPattern =
        """[$][{](([^}:]+\b)(?:[:]((?:(?>[^}\\]+)|\\.)+))?)[}]""".toRegex()
    private val registeredAnimations: MutableMap<String, Animation> = ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER)

    override val animations: Map<String, Animation>
        get() = ImmutableMap.copyOf(registeredAnimations)

    private val textAnimationFileSequence: Sequence<File>
        get() = animationsFolder.listFiles()
            .asSequence()
            .filter { it.isFile }
            .filter { it.extension.equals("txt", ignoreCase = true) }

    override fun createAnimationsFolderIfNotExists() {
        if (animationsFolder.exists()) return

        animationsFolder.mkdir()

        saveAnimationResourceToDisk("left-to-right.txt")
        saveAnimationResourceToDisk("right-to-left.txt")
    }

    override fun loadAnimations() {
        textAnimationFileSequence.forEach {
            val name = it.nameWithoutExtension

            addAnimation(name, createAnimationFromTextFile(it))
        }
    }

    override fun createAnimationFromText(text: String): Animation {
        return createAnimationFromTextLines(*text.lines().toTypedArray())
    }

    override fun createAnimationFromTextLines(vararg lines: String): Animation {
        val animationFrames = lines
            .map {
                val matcher = textAnimationFramePattern.toPattern().matcher(it)

                if (matcher.matches()) {
                    val fadeIn = matcher.group(1).toInt()
                    val stay = matcher.group(2).toInt()
                    val fadeOut = matcher.group(3).toInt()
                    val text = matcher.group(4).color().replace("\\n", "\n")

                    StandardAnimationFrame(text, fadeIn, stay, fadeOut)
                } else {
                    StandardAnimationFrame(it.color())
                }
            }

        return Animation { animationFrames.iterator() }
    }

    override fun createAnimationFromTextFile(file: File): Animation {
        return createAnimationFromTextLines(*file.readLines().toTypedArray())
    }

    override fun addAnimation(name: String, animation: Animation) {
        registeredAnimations[name] = animation
    }

    override fun removeAnimation(name: String) {
        registeredAnimations.remove(name)
    }

    override fun containsAnimations(text: String) = text.contains(animationPattern)

    override fun containsAnimation(text: String, animation: String) = text.contains("\${$animation}", ignoreCase = true)

    override fun textToAnimationParts(text: String): List<AnimationPart<*>> {
        if (containsAnimations(text)) {
            val list: MutableList<AnimationPart<*>> = mutableListOf()
            val matcher = animationPattern.toPattern().matcher(text)

            var lastEnd = 0

            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                val fullAnimation = matcher.group()
                val animation = matcher.group(2)
                val hasParameter = matcher.groupCount() == 3

                val part: String = text.substring(lastEnd, start)

                if (part.isNotEmpty()) {
                    list.add(AnimationPart { part })
                }

                if (hasParameter && scriptService.scriptExists(animation)) {
                    val animationValue = matcher.group(3).replace("\\}", "}")
                    list.add(AnimationPart { scriptService.getScriptAnimation(animation, animationValue, withPlaceholders = true) })
                } else if (registeredAnimations.containsKey(animation)) {
                    list.add(AnimationPart { registeredAnimations[animation] })
                } else {
                    list.add(AnimationPart { fullAnimation })
                }

                lastEnd = end
            }

            if (lastEnd != text.length) {
                val part: String = text.substring(lastEnd, text.length)

                if (part.isNotEmpty()) {
                    list.add(AnimationPart { part })
                }
            }

            return list
        }

        return listOf(AnimationPart { text })
    }

    private fun saveAnimationResourceToDisk(fileName: String) {
        plugin.getResource("animations/$fileName")?.let { url ->
            File(animationsFolder, fileName).writeBytes(url.readBytes())
        }
    }
}
