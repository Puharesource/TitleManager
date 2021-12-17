package dev.tarkan.titlemanager.lib.sequences

class MarqueeSequence(private val text: String, private val width: Int = text.length, isLooping: Boolean = true) : TitleManagerSequence(isLooping) {
    override fun createSequence() = sequence {
        val chars = text.toCharArray()

        for (i in text.indices) {
            val marqueeText = StringBuilder()

            for (j in 0 until width) {
                marqueeText.append(chars[(i + j) % text.length])
            }

            yield(marqueeText.toString())
        }
    }
}
