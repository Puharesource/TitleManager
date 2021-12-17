package dev.tarkan.titlemanager.lib.sequences

class TextDeleteSequence(private val text: String, isLooping: Boolean = false) : TitleManagerSequence(isLooping) {
    override fun createSequence() = sequence {
        for (i in text.length .. 0) {
            yield(text.substring(0, text.length - i))
        }
    }
}
