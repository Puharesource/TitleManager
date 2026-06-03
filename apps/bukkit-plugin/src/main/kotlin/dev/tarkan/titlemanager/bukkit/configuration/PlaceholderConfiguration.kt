package dev.tarkan.titlemanager.bukkit.configuration

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class PlaceholderConfiguration(
    val locale: String = "en-US",
    val numberFormat: NumberFormatConfiguration = NumberFormatConfiguration(),
    val dateFormat: String = "EEE, dd MMM yyyy HH:mm:ss z",
    val aliases: Map<String, String> = mapOf()
) {
    @Transient
    val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.forLanguageTag(locale))
}

@SerialName("NumberFormatConfiguration")
@Serializable
data class NumberFormatConfiguration(
    val enabled: Boolean = true,
    val format: String = "#,###.##"
) {
    init {
        DecimalFormat(format, DecimalFormatSymbols(Locale.ROOT))
    }
}
