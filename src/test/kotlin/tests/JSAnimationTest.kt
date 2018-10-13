package tests

import io.puharesource.mc.titlemanager.APIProvider
import io.puharesource.mc.titlemanager.script.GraalScriptManager
import io.puharesource.mc.titlemanager.script.NashornScriptManager
import io.puharesource.mc.titlemanager.script.ScriptManager
import org.graalvm.polyglot.Value
import org.junit.Before
import org.junit.Test
import java.lang.RuntimeException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JSAnimationTest {
    private lateinit var scriptManager: ScriptManager

    @Before
    fun init() {
        this.scriptManager = ScriptManager.create()!!
    }

    private fun toResult(animation: String, text: String, index: Int) : Pair<String, Boolean> {
        val result = scriptManager.getFrameFromScript(animation, text, index)
        val prefix = "(Animation: $animation | Index: $index)"

        assertEquals(result.size, 5, "$prefix Must return 5 elements.")

        if (scriptManager is GraalScriptManager) {
            result as Array<Value>

            assertTrue(result[0].isString, "$prefix Element 0 must be of type String.")
            assertTrue(result[1].isBoolean, "$prefix Element 1 must be of type Boolean.")
            assertTrue(result[2].isNumber, "$prefix Element 2 must be of type Int.")
            assertTrue(result[3].isNumber, "$prefix Element 3 must be of type Int.")
            assertTrue(result[4].isNumber, "$prefix Element 4 must be of type Int.")

            return Pair("${result[2]}, ${result[3]}, ${result[4]} | ${result[1]} | \"${result[0]}\"", result[1].asBoolean())
        } else if (scriptManager is NashornScriptManager) {
            assertTrue(result[0] is String, "$prefix Element 0 must be of type String.")
            assertTrue(result[1] is Boolean, "$prefix Element 1 must be of type Boolean.")
            assertTrue(result[2] is Int, "$prefix Element 2 must be of type Int.")
            assertTrue(result[3] is Int, "$prefix Element 3 must be of type Int.")
            assertTrue(result[4] is Int, "$prefix Element 4 must be of type Int.")

            return Pair("${result[2]}, ${result[3]}, ${result[4]} | ${result[1]} | \"${result[0]}\"", result[1] as Boolean)
        }

        throw RuntimeException("ScriptManager does not exist")
    }

    private fun toResultList(animation: String, text: String) : List<String> {
        val list : MutableList<String> = mutableListOf()

        var isDone = false
        var i = 0
        while (!isDone) {
            val result = toResult(animation, text, i)

            list.add(result.first)

            isDone = result.second
            i++
        }

        return list
    }

    private fun toExpectedResultList(expected: String) : List<String> {
        return expected.lines().map(String::trim).filter(String::isNotBlank)
    }

    private fun checkList(animation: String, results: List<String>, expectedResults: List<String>) {
        (0 until results.size).forEach {
            val result = results[it]
            val expectedResult = expectedResults[it]

            assertEquals(result, expectedResult, "(Animation $animation | Index: $it) Expected result \"$expectedResult\" but got \"$result\" instead.")
        }
    }

    @Test
    fun countdownTest() {
        val results = toResultList("count_down", "[1;2;3]10")

        assertEquals(results.size, 10, "Expected amount of frames is 10, received ${results.size} instead.")

        val expectedResults = toExpectedResultList(
        """
        1, 2, 3 | false | "10"
        1, 2, 3 | false | "9"
        1, 2, 3 | false | "8"
        1, 2, 3 | false | "7"
        1, 2, 3 | false | "6"
        1, 2, 3 | false | "5"
        1, 2, 3 | false | "4"
        1, 2, 3 | false | "3"
        1, 2, 3 | false | "2"
        1, 2, 3 | true | "1"
        """)

        checkList("count_down", results, expectedResults)
    }

    @Test
    fun countUpTest() {
        val results = toResultList("count_up", "[1;2;3]10")

        assertEquals(results.size, 10, "Expected amount of frames is 10, received ${results.size} instead.")

        val expectedResults = toExpectedResultList(
        """
        1, 2, 3 | false | "1"
        1, 2, 3 | false | "2"
        1, 2, 3 | false | "3"
        1, 2, 3 | false | "4"
        1, 2, 3 | false | "5"
        1, 2, 3 | false | "6"
        1, 2, 3 | false | "7"
        1, 2, 3 | false | "8"
        1, 2, 3 | false | "9"
        1, 2, 3 | true | "10"
        """)

        checkList("count_up", results, expectedResults)
    }

    @Test
    fun shineTest() {
        val results = toResultList("shine", "[1;2;3][4;5;6][7;8;9][&a;&1]Test String")

        assertEquals(results.size, 15, "Expected amount of frames is 15, received ${results.size} instead.")

        val expectedResults = toExpectedResultList(
        """
        4, 5, 6 | false | "&aTest String"
        1, 2, 3 | false | "&a&1T&aest String"
        1, 2, 3 | false | "&a&1Te&ast String"
        1, 2, 3 | false | "&a&1Tes&at String"
        1, 2, 3 | false | "&aT&1est&a String"
        1, 2, 3 | false | "&aTe&1st &aString"
        1, 2, 3 | false | "&aTes&1t S&atring"
        1, 2, 3 | false | "&aTest&1 St&aring"
        1, 2, 3 | false | "&aTest &1Str&aing"
        1, 2, 3 | false | "&aTest S&1tri&ang"
        1, 2, 3 | false | "&aTest St&1rin&ag"
        1, 2, 3 | false | "&aTest Str&1ing&a"
        1, 2, 3 | false | "&aTest Stri&1ng&a"
        1, 2, 3 | false | "&aTest Strin&1g&a"
        7, 8, 9 | true | "&aTest String"
        """)

        checkList("shine", results, expectedResults)
    }

    @Test
    fun textWriteTest() {
        val results = toResultList("text_write", "[1;2;3]Test String")

        assertEquals(results.size, 12, "Expected amount of frames is 12, received ${results.size} instead.")

        val expectedResults = toExpectedResultList(
        """
        1, 2, 3 | false | ""
        1, 2, 3 | false | "T"
        1, 2, 3 | false | "Te"
        1, 2, 3 | false | "Tes"
        1, 2, 3 | false | "Test"
        1, 2, 3 | false | "Test "
        1, 2, 3 | false | "Test S"
        1, 2, 3 | false | "Test St"
        1, 2, 3 | false | "Test Str"
        1, 2, 3 | false | "Test Stri"
        1, 2, 3 | false | "Test Strin"
        1, 2, 3 | true | "Test String"
        """)

        checkList("text_write", results, expectedResults)
    }

    @Test
    fun textDeleteTest() {
        val results = toResultList("text_delete", "[1;2;3]Test String")

        assertEquals(results.size, 12, "Expected amount of frames is 12, received ${results.size} instead.")

        val expectedResults = toExpectedResultList(
        """
        1, 2, 3 | false | "Test String"
        1, 2, 3 | false | "est String"
        1, 2, 3 | false | "st String"
        1, 2, 3 | false | "t String"
        1, 2, 3 | false | " String"
        1, 2, 3 | false | "String"
        1, 2, 3 | false | "tring"
        1, 2, 3 | false | "ring"
        1, 2, 3 | false | "ing"
        1, 2, 3 | false | "ng"
        1, 2, 3 | false | "g"
        1, 2, 3 | true | ""
        """)

        checkList("text_delete", results, expectedResults)
    }

    @Test
    fun toAnimationTest() {
        fun test(text: String, expectedAmount: Int) {
            val parts = APIProvider.toAnimationParts(text)

            parts.forEach { println(text) }

            assertEquals(expectedAmount, parts.size, "Expected $expectedAmount animation parts, got ${parts.size} instead.")
        }

        test("\${count_up:3}", 1)
        test("\${count_up:3} test", 2)
        test("test \${count_up:3}", 2)
        test("test \${count_up:3} test", 3)
        test("\${count_up:3}\${count_up:3}", 2)
        test("\${count_up:3} \${count_up:3}", 3)
        test("\${count_up:3} test \${count_up:3}", 3)
        test("\${count_up:3}\${count_up:3} test", 3)
        test("test \${count_up:3}\${count_up:3}", 3)
        test("test \${count_up:3}\${count_up:3} test", 4)
        test("test \${count_up:3} test \${count_up:3} test", 5)
    }
}