package tests

import io.puharesource.mc.titlemanager.APIProvider
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import org.junit.Test
import testPlayer
import kotlin.test.assertTrue

class RegexTest {
    init {
        APIProvider.addPlaceholderReplacer("test", { player -> "test" })
        APIProvider.addPlaceholderReplacerWithValue("test1", { player, value -> "test1: $value" })
        APIProvider.addPlaceholderReplacerWithValue("test2", { player, value -> "test2: $value" })
        APIProvider.addAnimation("test-animation", { test -> listOf(APIProvider.createAnimationFrame("hello", 1, 2, 3), APIProvider.createAnimationFrame("der", 1, 2, 3)).iterator() })
    }

    @Test
    fun matches() {
        assertTrue(APIProvider.textAnimationFramePattern.toPattern().matcher("[1;2;3]Test").matches(), "Animation frame pattern doesn't match.")

        assertTrue(APIProvider.variablePattern.toPattern().matcher("%{test}").matches(), "Variable pattern doesn't match.")
        assertTrue(APIProvider.animationPattern.toPattern().matcher("\${test}").matches(), "Animation pattern doesn't match.")

        assertTrue(APIProvider.variablePatternWithParameter.toPattern().matcher("%{test:test test test}").matches(), "Variable with parameter pattern doesn't match.")
        assertTrue(APIProvider.animationPatternWithParameter.toPattern().matcher("\${test:test test test}").matches(), "Animation with parameter pattern doesn't match.")
    }

    @Test
    fun containsMatch() {
        assertTrue("test %{test} test".contains(APIProvider.variablePattern), "Variable pattern isn't contained within string.")
        assertTrue("test \${test} test".contains(APIProvider.animationPattern), "Animation pattern isn't contained within string.")

        assertTrue("test %{test:test test test} test".contains(APIProvider.variablePatternWithParameter), "Variable with parameter pattern isn't contained within string.")
        assertTrue("test \${test:test test test} test".contains(APIProvider.animationPatternWithParameter), "Animation with parameter pattern isn't contained within string.")
    }

    @Test
    fun containsMultipleVariables() {
        val test = "%{test1:uwot} %{test} %{test2:uwet}"

        println(test)
        println(APIProvider.replaceText(testPlayer, test))

        assertTrue { test.contains(APIProvider.variablePattern) }
        assertTrue { test.contains(APIProvider.variablePatternWithParameter) }
    }

    @Test
    fun containsMultipleAnimations() {
        val test = "\${shine:Shiiinneee plz}\${test-animation}"
        val parts = APIProvider.toAnimationParts(test)

        parts.forEach { println(it.part) }

        //assertTrue(test.contains(APIProvider.animationPatternWithParameter), "Animation with parameter pattern isn't contained within string.")
        //assertTrue(test.contains(APIProvider.animationPattern), "Animation pattern isn't contained within string.")
    }
}