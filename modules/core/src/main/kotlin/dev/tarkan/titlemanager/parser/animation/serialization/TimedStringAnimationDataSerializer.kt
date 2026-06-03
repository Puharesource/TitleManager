package dev.tarkan.titlemanager.parser.animation.serialization

import dev.tarkan.titlemanager.time.Timing

object TimedStringAnimationDataSerializer : AnimationDataSerializer<TimedStringAnimationDataSerializer.Data> {
    override fun serialize(data: Data?): String? {
        data ?: return null

        val text = data.text.orEmpty()
        val timing = data.timing ?: return data.text

        return "[${timing.fadeInMilliseconds};${timing.stayMilliseconds};${timing.fadeOutMilliseconds}]$text"
    }

    override fun deserialize(serializedString: String?): Data? {
        serializedString ?: return null

        val (timing, text) = Timing.splitTimingsAndText(serializedString)

        return Data(timing, text)
    }

    data class Data(val timing: Timing?, val text: String?)
}