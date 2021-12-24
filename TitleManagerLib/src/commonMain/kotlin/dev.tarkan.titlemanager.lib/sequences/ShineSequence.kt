package dev.tarkan.titlemanager.lib.sequences

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@JsExport
@ExperimentalJsExport
class ShineSequence(private val text: String, private val primaryColor: String, private val secondaryColor: String, isLooping: Boolean = true) : TitleManagerSequence(isLooping) {
    override fun createSequence() = sequence {
        for (i in 0..(text.length + 3)) {
            yield(manipulateText(i))
        }
    }

    private fun manipulateText(index: Int): String {
        if (index == 0 || index >= text.length + 3) {
            return primaryColor + text
        }

        val startIndex = (index - 3).coerceAtLeast(0)
        val endIndex = index.coerceAtMost(text.length)

        val left = primaryColor + text.substring(0, startIndex)
        val center = secondaryColor + text.substring(startIndex, endIndex)
        val right = primaryColor + text.substring(endIndex)

        return left + center + right
    }
}
