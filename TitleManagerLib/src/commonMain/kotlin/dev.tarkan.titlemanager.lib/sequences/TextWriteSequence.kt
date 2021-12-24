package dev.tarkan.titlemanager.lib.sequences

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@JsExport
@ExperimentalJsExport
class TextWriteSequence(private val text: String, isLooping: Boolean = false) : TitleManagerSequence(isLooping) {
    override fun createSequence() = sequence {
        for (i in 0..text.length) {
            yield(text.substring(0, i))
        }
    }
}
