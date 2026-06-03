package dev.tarkan.titlemanager.bukkit.animation

import dev.tarkan.titlemanager.animation.Animation
import dev.tarkan.titlemanager.bukkit.configuration.GradientConfiguration
import dev.tarkan.titlemanager.bukkit.configuration.GradientsConfiguration
import dev.tarkan.titlemanager.parser.animation.serialization.AnimationDataSerializer
import dev.tarkan.titlemanager.time.TimedItem
import dev.tarkan.titlemanager.time.Timing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ColorGradientAnimation<TContext>(private val timing: Timing?, private val gradient: GradientConfiguration, private val separator: String, private val text: String) : Animation<TContext, String> {
    override fun singleIterationFlowWithTimings(context: TContext, isInfinite: Boolean): Flow<TimedItem<String>> = flow {
        val colors = if (isInfinite) gradient.cachedRadialColors else gradient.cachedColors

        for (i in colors.indices) {
            val coloredText = buildString {
                for ((textIndex, c) in text.withIndex()) {
                    val colorIndex = (i + textIndex) % colors.size
                    val color = colors[colorIndex]

                    append(color)
                    append(separator)
                    append(c)
                }
            }

            emit(TimedItem(Timing.createStatic(50u), coloredText))
            delay(timing?.totalMilliseconds?.toLong() ?: 50L)
        }
    }

    data class Data(val timing: Timing? = null, val gradientName: String, val gradient: GradientConfiguration, val separator: String, val text: String)

    class DataSerializer(private val config: GradientsConfiguration) : AnimationDataSerializer<Data> {
        private companion object {
            val FULL_LINE_REGEX: Regex = """^\[(?<gradientName>.+?)(?:;(?<separator>.+))?](?<text>.+)${'$'}""".toRegex()
        }

        override fun serialize(data: Data?): String {
            requireNotNull(data) { "No data found" }

            return buildString {
                if (data.timing != null) {
                    append(data.timing.toSerializedString())
                }

                append(data.gradientName)
            }
        }

        override fun deserialize(serializedString: String?): Data {
            requireNotNull(serializedString) { "No gradient found" }

            val timingSplit = Timing.splitTimingsAndText(serializedString)
            val match = FULL_LINE_REGEX.matchEntire(timingSplit.second) ?: throw IllegalArgumentException("Invalid syntax for gradient")

            val gradientName = match.groups["gradientName"]?.value ?: throw IllegalArgumentException("No gradient name found")
            val separator = match.groups["separator"]?.value ?: ""
            val text = match.groups["text"]?.value ?: "Invalid-Data"

            val gradient = config.findGradient(gradientName) ?: throw IllegalArgumentException("No gradient with name '${gradientName}' found")

            return Data(
                timing = timingSplit.first,
                gradientName = gradientName,
                separator = separator,
                gradient = gradient,
                text = text
            )
        }

        private fun Timing.toSerializedString() = "[$fadeInMilliseconds;$stayMilliseconds;$fadeOutMilliseconds]"
    }

}