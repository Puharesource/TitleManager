import dev.tarkan.titlemanager.animation.TimelineAnimation
import dev.tarkan.titlemanager.bukkit.animation.addAnimationFilePlaceholders
import dev.tarkan.titlemanager.parser.IntermediaryParser
import dev.tarkan.titlemanager.parser.animation.AnimationParser
import dev.tarkan.titlemanager.parser.placeholder.animation.AnimationPlaceholderRegistry
import dev.tarkan.titlemanager.parser.placeholder.animation.addCoreAnimationPlaceholders
import dev.tarkan.titlemanager.parser.placeholder.variable.VariablePlaceholderRegistry
import java.nio.file.Files
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AnimationFilePlaceholdersTest {
    @Test
    fun `loads old TitleManager animation file as placeholder`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-animation-files")

        try {
            val animationFile = tempDirectory.resolve("left-to-right.txt")
            animationFile.writeText(
                """
                    [0;5;0]&7&b-&7---------
                    [0;2;0]&7-&b-&7--------
                    [0;2;0]&7--&b-&7-------
                    [0;2;0]&7---&b-&7------
                    [0;2;0]&7----&b-&7-----
                    [0;2;0]&7-----&b-&7----
                    [0;2;0]&7------&b-&7---
                    [0;2;0]&7-------&b-&7--
                    [0;2;0]&7--------&b-&7-
                    [0;5;0]&7---------&b-&7
                    [0;2;0]&7--------&b-&7-
                    [0;2;0]&7-------&b-&7--
                    [0;2;0]&7------&b-&7---
                    [0;2;0]&7-----&b-&7----
                    [0;2;0]&7----&b-&7-----
                    [0;2;0]&7---&b-&7------
                    [0;2;0]&7--&b-&7-------
                    [0;2;0]&7-&b-&7--------
                """.trimIndent()
            )

            val intermediaryParser = IntermediaryParser(50u)
            val variablePlaceholderRegistry = VariablePlaceholderRegistry.build<Unit> {}
            lateinit var animationPlaceholderRegistry: AnimationPlaceholderRegistry<Unit>

            animationPlaceholderRegistry = AnimationPlaceholderRegistry.build {
                addAnimationFilePlaceholders(listOf(animationFile), intermediaryParser) {
                    AnimationParser(variablePlaceholderRegistry, animationPlaceholderRegistry)
                }
            }

            val placeholder = assertNotNull(animationPlaceholderRegistry["left-to-right"])
            val animation = placeholder.compile(null)
            val timeline = (animation as TimelineAnimation<Unit, String>).singleIterationTimeline(Unit)

            assertEquals(18, timeline.frames.size)
            assertEquals("&7&b-&7---------", timeline.frames.first().item)
            assertEquals(250.0, timeline.frames.first().timing.stayMilliseconds.toDouble())
            assertEquals("&7-&b-&7--------", timeline.frames.last().item)
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `loaded animation files can use shared core animation placeholders`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-animation-files")

        try {
            val animationFile = tempDirectory.resolve("welcome-cycle.txt")
            animationFile.writeText("[1]${'$'}{text_write:Hi}")

            val intermediaryParser = IntermediaryParser(50u)
            val variablePlaceholderRegistry = VariablePlaceholderRegistry.build<Unit> {}
            lateinit var animationPlaceholderRegistry: AnimationPlaceholderRegistry<Unit>

            animationPlaceholderRegistry = AnimationPlaceholderRegistry.build {
                addCoreAnimationPlaceholders()
                addAnimationFilePlaceholders(listOf(animationFile), intermediaryParser) {
                    AnimationParser(variablePlaceholderRegistry, animationPlaceholderRegistry)
                }
            }

            val placeholder = assertNotNull(animationPlaceholderRegistry["welcome-cycle"])
            val animation = placeholder.compile(null)
            val timeline = (animation as TimelineAnimation<Unit, String>).singleIterationTimeline(Unit)

            assertEquals(listOf("", "H", "Hi"), timeline.frames.map { it.item })
            assertTrue(timeline.totalMilliseconds!! > 0L)
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }
}
