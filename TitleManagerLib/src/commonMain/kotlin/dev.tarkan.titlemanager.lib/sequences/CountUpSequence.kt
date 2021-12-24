package dev.tarkan.titlemanager.lib.sequences

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@JsExport
@ExperimentalJsExport
class CountUpSequence(private val to: Int, isLooping: Boolean = false) : TitleManagerSequence(isLooping) {
    override fun createSequence() = sequence {
        for (i in 1..to) {
            yield(i.toString())
        }
    }
}
