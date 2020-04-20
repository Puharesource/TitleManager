package io.puharesource.mc.titlemanager.internal.model.command

import io.puharesource.mc.titlemanager.internal.extensions.isDouble
import io.puharesource.mc.titlemanager.internal.extensions.isInt

data class CommandParameter(val parameter: String, val value: String? = null) {
    fun hasValue() = value != null

    fun getValueOr(other: String): String {
        if (value == null) {
            return other
        }

        return value
    }

    fun getIntOr(other: Int): Int {
        if (value == null || !value.isInt()) {
            return other
        }

        return value.toInt()
    }

    fun getIntOrNull(): Int? {
        if (value == null || !value.isInt()) {
            return null
        }

        return value.toInt()
    }

    fun getDoubleOr(other: Double): Double {
        if (value == null || !value.isDouble()) {
            return other
        }

        return value.toDouble()
    }

    fun getDoubleOrNull(): Double? {
        if (value == null || !value.isDouble()) {
            return null
        }

        return value.toDouble()
    }
}
