package io.puharesource.mc.titlemanager.internal.color

fun interface Interpolator<T> {
    fun interpolate(percentage: Float): T
}
