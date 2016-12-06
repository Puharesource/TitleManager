package tests

import io.puharesource.mc.titlemanager.APIProvider
import org.junit.Test
import kotlin.test.assertTrue

class RegexTest {
    @Test
    fun containsMatch() {
        assertTrue("test %{test} test".contains(APIProvider.variablePattern), "Variable pattern isn't contained within string.")
        assertTrue("test \${test} test".contains(APIProvider.animationPattern), "Animation pattern isn't contained within string.")

        assertTrue("test %{test:test test test} test".contains(APIProvider.variablePatternWithParameter), "Variable with parameter pattern isn't contained within string.")
        assertTrue("test \${test:test test test} test".contains(APIProvider.animationPatternWithParameter), "Animation with parameter pattern isn't contained within string.")
    }

    @Test
    fun matches() {
        assertTrue(APIProvider.textAnimationFramePattern.toPattern().matcher("[1;2;3]Test").matches(), "Animation frame pattern doesn't match.")

        assertTrue(APIProvider.variablePattern.toPattern().matcher("%{test}").matches(), "Variable pattern doesn't match.")
        assertTrue(APIProvider.animationPattern.toPattern().matcher("\${test}").matches(), "Animation pattern doesn't match.")

        assertTrue(APIProvider.variablePatternWithParameter.toPattern().matcher("%{test:test test test}").matches(), "Variable with parameter pattern doesn't match.")
        assertTrue(APIProvider.animationPatternWithParameter.toPattern().matcher("\${test:test test test}").matches(), "Animation with parameter pattern doesn't match.")
    }
}