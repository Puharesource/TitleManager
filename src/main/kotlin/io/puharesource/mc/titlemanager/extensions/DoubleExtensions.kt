package io.puharesource.mc.titlemanager.extensions

import io.puharesource.mc.titlemanager.pluginInstance
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

internal fun Double.format() : String {
    val section = pluginInstance.config.getConfigurationSection("placeholders.number-format")

    if (section.getBoolean("enabled")) {
        val decimal = BigDecimal(this)
        val format = section.getString("format")
        val symbols = DecimalFormatSymbols(Locale.US)

        return DecimalFormat(format, symbols).format(decimal)
    }

    return toString()
}