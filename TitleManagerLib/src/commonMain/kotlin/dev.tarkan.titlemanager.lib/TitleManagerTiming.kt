package dev.tarkan.titlemanager.lib

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@JsExport
@ExperimentalJsExport
data class TitleManagerTiming(val fadeIn: Int, val stay: Int, val fadeOut: Int, val unit: DurationUnit = DurationUnit.MILLISECONDS) {
    val total: Int
        get() = fadeIn + stay + fadeOut

    @JsName("convertFadeIn")
    fun fadeIn(toUnit: DurationUnit): Int = Duration.convert(fadeIn.toDouble(), unit, toUnit).toInt()

    @JsName("convertStay")
    fun stay(toUnit: DurationUnit): Int = Duration.convert(stay.toDouble(), unit, toUnit).toInt()

    @JsName("convertFadeOut")
    fun fadeOut(toUnit: DurationUnit): Int = Duration.convert(fadeOut.toDouble(), unit, toUnit).toInt()

    @JsName("convertTotal")
    fun total(toUnit: DurationUnit): Int = Duration.convert(total.toDouble(), unit, toUnit).toInt()
}
