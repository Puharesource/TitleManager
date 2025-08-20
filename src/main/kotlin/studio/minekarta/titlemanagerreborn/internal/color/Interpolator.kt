package studio.minekarta.titlemanagerreborn.internal.color

fun interface Interpolator<T> {
    fun interpolate(percentage: Float): T
}
