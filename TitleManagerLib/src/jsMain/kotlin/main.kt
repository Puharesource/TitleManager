import dev.tarkan.titlemanager.lib.sequences.CountDownSequence
import dev.tarkan.titlemanager.lib.sequences.CountUpSequence
import dev.tarkan.titlemanager.lib.sequences.MarqueeSequence
import dev.tarkan.titlemanager.lib.sequences.ShineSequence
import dev.tarkan.titlemanager.lib.sequences.TextDeleteSequence
import dev.tarkan.titlemanager.lib.sequences.TextWriteSequence
import kotlinx.browser.window

@JsExport
@ExperimentalJsExport
fun countdown(from: Int, isLooping: Boolean = false, callback: (String) -> Unit): () -> Unit {
    val iterator = CountDownSequence(from, isLooping).sequence.iterator()

    return delayedLoop(1000, iterator, callback)
}

@JsExport
@ExperimentalJsExport
fun count(to: Int, isLooping: Boolean = false, callback: (String) -> Unit): () -> Unit {
    val iterator = CountUpSequence(to, isLooping).sequence.iterator()

    return delayedLoop(1000, iterator, callback)
}

@JsExport
@ExperimentalJsExport
fun writeText(text: String, interval: Int, isLooping: Boolean = false, callback: (String) -> Unit): () -> Unit {
    val iterator = TextWriteSequence(text, isLooping).sequence.iterator()

    return delayedLoop(interval, iterator, callback)
}

@JsExport
@ExperimentalJsExport
fun deleteText(text: String, interval: Int, isLooping: Boolean = false, callback: (String) -> Unit): () -> Unit {
    val iterator = TextDeleteSequence(text, isLooping).sequence.iterator()

    return delayedLoop(interval, iterator, callback)
}

@JsExport
@ExperimentalJsExport
fun marquee(text: String, interval: Int, isLooping: Boolean = false, callback: (String) -> Unit): () -> Unit {
    val iterator = MarqueeSequence(text, isLooping = isLooping).sequence.iterator()

    return delayedLoop(interval, iterator, callback)
}

@JsExport
@ExperimentalJsExport
fun shine(text: String, primaryColor: String, secondaryColor: String, interval: Int, isLooping: Boolean = false, callback: (String) -> Unit): () -> Unit {
    val iterator = ShineSequence(text, primaryColor, secondaryColor, isLooping).sequence.iterator()

    return delayedLoop(interval, iterator, callback)
}

private fun delayedLoop(delay: Int, iterator: Iterator<String>, callback: (String) -> Unit, setId: ((id: Int) -> Unit)? = null): () -> Unit {
    var timeoutId = -1

    if (iterator.hasNext()) {
        callback(iterator.next())

        if (iterator.hasNext()) {
            timeoutId = window.setTimeout({ delayedLoop(delay, iterator, callback) { timeoutId = it } }, delay)

            setId?.invoke(timeoutId)
        }
    }

    return {
        if (timeoutId != -1) {
            window.clearTimeout(timeoutId)
        }
    }
}
