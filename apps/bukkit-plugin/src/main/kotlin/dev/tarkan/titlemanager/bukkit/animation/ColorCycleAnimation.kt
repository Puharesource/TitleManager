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

class ColorCycleAnimation<TContext>(private val timing: Timing? = null, private val gradient: GradientConfiguration) : Animation<TContext, String> {
    override fun singleIterationFlowWithTimings(context: TContext, isInfinite: Boolean): Flow<TimedItem<String>> = flow {
        val colors = if (isInfinite) gradient.cachedRadialColors else gradient.cachedColors

        for (color in colors) {
            emit(TimedItem(Timing.createStatic(50u), color))
            delay(timing?.totalMilliseconds?.toLong() ?: 50L)
        }
    }

    data class Data(val timing: Timing? = null, val gradientName: String, val gradient: GradientConfiguration)

    class DataSerializer(private val config: GradientsConfiguration) : AnimationDataSerializer<Data> {
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
            val gradient = config.findGradient(timingSplit.second) ?: throw IllegalArgumentException("No gradient with name '${timingSplit.second}' found.")

            return Data(
                timing = timingSplit.first,
                gradientName = timingSplit.second,
                gradient = gradient
            )
        }

        private fun Timing.toSerializedString() = "[$fadeInMilliseconds;$stayMilliseconds;$fadeOutMilliseconds]"
    }

}