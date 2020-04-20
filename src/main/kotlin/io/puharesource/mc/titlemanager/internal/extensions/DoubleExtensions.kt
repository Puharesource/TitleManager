package io.puharesource.mc.titlemanager.internal.extensions

import io.puharesource.mc.titlemanager.internal.pluginConfig
import java.math.BigDecimal

internal fun Double.format(): String {
    val numberFormat = pluginConfig.placeholders.numberFormat

    if (numberFormat.enabled) {
        val decimal = BigDecimal(this)

        return numberFormat.format.format(decimal)
    }

    return toString()
}
