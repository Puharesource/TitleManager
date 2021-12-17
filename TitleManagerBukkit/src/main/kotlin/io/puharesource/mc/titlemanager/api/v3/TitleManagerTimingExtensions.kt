package io.puharesource.mc.titlemanager.api.v3

import dev.tarkan.titlemanager.lib.TitleManagerTiming
import kotlin.time.DurationUnit

fun TitleManagerTiming.fadeInTicks() = fadeIn(DurationUnit.MILLISECONDS) * 50
fun TitleManagerTiming.stayTicks() = stay(DurationUnit.MILLISECONDS) * 50
fun TitleManagerTiming.fadeOutTicks() = fadeOut(DurationUnit.MILLISECONDS) * 50
fun TitleManagerTiming.totalTicks() = total(DurationUnit.MILLISECONDS) * 50
