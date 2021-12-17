package dev.tarkan.titlemanager.lib.sequences

abstract class TitleManagerSequence(val isLooping: Boolean) {
    var isDone: Boolean = false

    val sequence by lazy {
        sequence {
            var running = true
            var seq = createSequence()

            while (running && !isDone) {
                seq.forEach {
                    yield(it)
                }

                if (isLooping) {
                    seq = createSequence()
                } else {
                    running = false
                }
            }

            isDone = true
        }
    }

    abstract fun createSequence(): Sequence<String>
}
