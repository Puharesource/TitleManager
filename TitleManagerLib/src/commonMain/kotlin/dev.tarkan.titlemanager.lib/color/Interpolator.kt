package dev.tarkan.titlemanager.lib.color

fun interface Interpolator<T> {
    fun interpolate(percentage: Float): T
}
