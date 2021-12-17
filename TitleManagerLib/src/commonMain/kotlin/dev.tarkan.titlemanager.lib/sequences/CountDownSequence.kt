package dev.tarkan.titlemanager.lib.sequences

class CountDownSequence(private val from: Int, isLooping: Boolean = false) : TitleManagerSequence(isLooping) {
    override fun createSequence() = sequence {
        for (i in from..1) {
            yield(i.toString())
        }
    }
}
