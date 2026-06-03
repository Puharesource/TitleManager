package dev.tarkan.titlemanager.parser.placeholder.animation

import dev.tarkan.titlemanager.animation.CountdownAnimation
import dev.tarkan.titlemanager.animation.MarqueeAnimation
import dev.tarkan.titlemanager.animation.ShineAnimation
import dev.tarkan.titlemanager.animation.TextDeleteAnimation
import dev.tarkan.titlemanager.animation.TextWriteAnimation
import dev.tarkan.titlemanager.parser.animation.serialization.IntAnimationDataSerializer
import dev.tarkan.titlemanager.parser.animation.serialization.StringAnimationDataSerializer

fun <TContext> AnimationPlaceholderRegistry.Builder<TContext>.addCoreAnimationPlaceholders():
    AnimationPlaceholderRegistry.Builder<TContext> {
    addSimple("countdown", dataSerializer = IntAnimationDataSerializer) { n ->
        CountdownAnimation(n ?: 0)
    }

    addSimple("text_write", dataSerializer = StringAnimationDataSerializer) {
        TextWriteAnimation(it ?: "")
    }

    addSimple("text_delete", dataSerializer = StringAnimationDataSerializer) {
        TextDeleteAnimation(it ?: "")
    }

    addSimple("marquee", dataSerializer = MarqueeAnimation.DataSerializer) { data ->
        val dataOrDefault = data ?: MarqueeAnimation.Data()

        MarqueeAnimation(dataOrDefault.text, dataOrDefault.timing, dataOrDefault.width)
    }

    addSimple("shine", dataSerializer = ShineAnimation.DataSerializer) { data ->
        val dataOrDefault = data ?: ShineAnimation.Data()

        ShineAnimation(
            dataOrDefault.text,
            dataOrDefault.primaryColor,
            dataOrDefault.secondaryColor,
            dataOrDefault.intermediaryTiming,
            dataOrDefault.startTiming,
            dataOrDefault.endTiming
        )
    }

    return this
}
