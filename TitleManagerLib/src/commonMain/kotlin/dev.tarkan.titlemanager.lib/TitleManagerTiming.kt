package dev.tarkan.titlemanager.lib

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
data class TitleManagerTiming(val fadeIn: Int, val stay: Int, val fadeOut: Int, val unit: DurationUnit = DurationUnit.MILLISECONDS) {
    val total: Int
        get() = fadeIn + stay + fadeOut

    fun fadeIn(toUnit: DurationUnit): Int = Duration.convert(fadeIn.toDouble(), unit, toUnit).toInt()
    fun stay(toUnit: DurationUnit): Int = Duration.convert(stay.toDouble(), unit, toUnit).toInt()
    fun fadeOut(toUnit: DurationUnit): Int = Duration.convert(fadeOut.toDouble(), unit, toUnit).toInt()
    fun total(toUnit: DurationUnit): Int = Duration.convert(total.toDouble(), unit, toUnit).toInt()
}
