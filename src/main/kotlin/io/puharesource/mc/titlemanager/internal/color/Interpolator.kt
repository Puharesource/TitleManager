package io.puharesource.mc.titlemanager.internal.color

interface Interpolator<T> {
    fun interpolate(percentage: Float): T
}
