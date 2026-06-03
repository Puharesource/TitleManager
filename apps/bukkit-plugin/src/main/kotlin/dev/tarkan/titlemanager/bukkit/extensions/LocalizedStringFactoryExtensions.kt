package dev.tarkan.titlemanager.bukkit.extensions

import de.comahe.i18n4k.Locale
import de.comahe.i18n4k.strings.*
import dev.tarkan.titlemanager.bukkit.text.ComponentSerializer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.TextColor

private val PARAMETERS = (0..9)
    .map { "{$it}" }

private const val PARAMETER_LENGTH = 3

sealed class MessageColors {
    companion object {
        val PRIMARY = TextColor.fromHexString("#58e2bb")!!
        val SECONDARY = TextColor.fromHexString("#e2587f")!!

        val ERROR = TextColor.fromHexString("#7d2e51")!!
        val ERROR_SECONDARY = TextColor.fromHexString("#00693a")!!
    }
}

fun LocalizedString.toComponent(locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(primaryColor = MessageColors.PRIMARY, secondaryColor = MessageColors.SECONDARY)

fun LocalizedStringFactory1.toComponent(p0: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, primaryColor = MessageColors.PRIMARY, secondaryColor = MessageColors.SECONDARY)

fun LocalizedStringFactory2.toComponent(p0: Any, p1: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, p1, primaryColor = MessageColors.PRIMARY, secondaryColor = MessageColors.SECONDARY)

fun LocalizedStringFactory3.toComponent(p0: Any, p1: Any, p2: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, p1, p2, primaryColor = MessageColors.PRIMARY, secondaryColor = MessageColors.SECONDARY)

fun LocalizedStringFactory4.toComponent(p0: Any, p1: Any, p2: Any, p3: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, p1, p2, p3, primaryColor = MessageColors.PRIMARY, secondaryColor = MessageColors.SECONDARY)

fun LocalizedStringFactory5.toComponent(p0: Any, p1: Any, p2: Any, p3: Any, p4: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, p1, p2, p3, p4, primaryColor = MessageColors.PRIMARY, secondaryColor = MessageColors.SECONDARY)

fun LocalizedStringFactory6.toComponent(p0: Any, p1: Any, p2: Any, p3: Any, p4: Any, p5: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, p1, p2, p3, p4, p5, primaryColor = MessageColors.PRIMARY, secondaryColor = MessageColors.SECONDARY)

fun LocalizedStringFactory7.toComponent(p0: Any, p1: Any, p2: Any, p3: Any, p4: Any, p5: Any, p6: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, p1, p2, p3, p4, p5, p6, primaryColor = MessageColors.PRIMARY, secondaryColor = MessageColors.SECONDARY)

fun LocalizedStringFactory8.toComponent(p0: Any, p1: Any, p2: Any, p3: Any, p4: Any, p5: Any, p6: Any, p7: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, p1, p2, p3, p4, p5, p6, p7, primaryColor = MessageColors.PRIMARY, secondaryColor = MessageColors.SECONDARY)

fun LocalizedStringFactory9.toComponent(p0: Any, p1: Any, p2: Any, p3: Any, p4: Any, p5: Any, p6: Any, p7: Any, p8: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, p1, p2, p3, p4, p5, p6, p7, p8, primaryColor = MessageColors.PRIMARY, secondaryColor = MessageColors.SECONDARY)

fun LocalizedStringFactory10.toComponent(p0: Any, p1: Any, p2: Any, p3: Any, p4: Any, p5: Any, p6: Any, p7: Any, p8: Any, p9: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, primaryColor = MessageColors.PRIMARY, secondaryColor = MessageColors.SECONDARY)

fun LocalizedString.toErrorComponent(locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(primaryColor = MessageColors.ERROR, secondaryColor = MessageColors.ERROR_SECONDARY)

fun LocalizedStringFactory1.toErrorComponent(p0: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, primaryColor = MessageColors.ERROR, secondaryColor = MessageColors.ERROR_SECONDARY)

fun LocalizedStringFactory2.toErrorComponent(p0: Any, p1: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, p1, primaryColor = MessageColors.ERROR, secondaryColor = MessageColors.ERROR_SECONDARY)

fun LocalizedStringFactory3.toErrorComponent(p0: Any, p1: Any, p2: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, p1, p2, primaryColor = MessageColors.ERROR, secondaryColor = MessageColors.ERROR_SECONDARY)

fun LocalizedStringFactory4.toErrorComponent(p0: Any, p1: Any, p2: Any, p3: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, p1, p2, p3, primaryColor = MessageColors.ERROR, secondaryColor = MessageColors.ERROR_SECONDARY)

fun LocalizedStringFactory5.toErrorComponent(p0: Any, p1: Any, p2: Any, p3: Any, p4: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, p1, p2, p3, p4, primaryColor = MessageColors.ERROR, secondaryColor = MessageColors.ERROR_SECONDARY)

fun LocalizedStringFactory6.toErrorComponent(p0: Any, p1: Any, p2: Any, p3: Any, p4: Any, p5: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, p1, p2, p3, p4, p5, primaryColor = MessageColors.ERROR, secondaryColor = MessageColors.ERROR_SECONDARY)

fun LocalizedStringFactory7.toErrorComponent(p0: Any, p1: Any, p2: Any, p3: Any, p4: Any, p5: Any, p6: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, p1, p2, p3, p4, p5, p6, primaryColor = MessageColors.ERROR, secondaryColor = MessageColors.ERROR_SECONDARY)

fun LocalizedStringFactory8.toErrorComponent(p0: Any, p1: Any, p2: Any, p3: Any, p4: Any, p5: Any, p6: Any, p7: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, p1, p2, p3, p4, p5, p6, p7, primaryColor = MessageColors.ERROR, secondaryColor = MessageColors.ERROR_SECONDARY)

fun LocalizedStringFactory9.toErrorComponent(p0: Any, p1: Any, p2: Any, p3: Any, p4: Any, p5: Any, p6: Any, p7: Any, p8: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, p1, p2, p3, p4, p5, p6, p7, p8, primaryColor = MessageColors.ERROR, secondaryColor = MessageColors.ERROR_SECONDARY)

fun LocalizedStringFactory10.toErrorComponent(p0: Any, p1: Any, p2: Any, p3: Any, p4: Any, p5: Any, p6: Any, p7: Any, p8: Any, p9: Any, locale: Locale) = GenericLocalizedStringFactory(this, locale)
    .toComponent(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, primaryColor = MessageColors.ERROR, secondaryColor = MessageColors.ERROR_SECONDARY)

private class GenericLocalizedStringFactory {
    private val template: String
    private val parameters: List<String>

    constructor(template: String, parameterCount: Int) {
        this.template = template
        this.parameters = (0..<parameterCount).map { PARAMETERS[it] }
    }

    constructor(handle: LocalizedString, locale: Locale)
            : this(handle.toString(locale), 0)

    constructor(handle: LocalizedStringFactory1, locale: Locale)
            : this(handle.createString(PARAMETERS[0], locale), 1)

    constructor(handle: LocalizedStringFactory2, locale: Locale)
            : this(handle.createString(PARAMETERS[0], PARAMETERS[1], locale), 2)

    constructor(handle: LocalizedStringFactory3, locale: Locale)
            : this(handle.createString(PARAMETERS[0], PARAMETERS[1], PARAMETERS[2], locale), 3)

    constructor(handle: LocalizedStringFactory4, locale: Locale)
            : this(handle.createString(PARAMETERS[0], PARAMETERS[1], PARAMETERS[2], PARAMETERS[3], locale), 4)

    constructor(handle: LocalizedStringFactory5, locale: Locale)
            : this(handle.createString(PARAMETERS[0], PARAMETERS[1], PARAMETERS[2], PARAMETERS[3], PARAMETERS[4], locale), 5)

    constructor(handle: LocalizedStringFactory6, locale: Locale)
            : this(handle.createString(PARAMETERS[0], PARAMETERS[1], PARAMETERS[2], PARAMETERS[3], PARAMETERS[4], PARAMETERS[5], locale), 6)

    constructor(handle: LocalizedStringFactory7, locale: Locale)
            : this(handle.createString(PARAMETERS[0], PARAMETERS[1], PARAMETERS[2], PARAMETERS[3], PARAMETERS[4], PARAMETERS[5], PARAMETERS[6], locale), 7)

    constructor(handle: LocalizedStringFactory8, locale: Locale)
            : this(handle.createString(PARAMETERS[0], PARAMETERS[1], PARAMETERS[2], PARAMETERS[3], PARAMETERS[4], PARAMETERS[5], PARAMETERS[6], PARAMETERS[7], locale), 8)

    constructor(handle: LocalizedStringFactory9, locale: Locale)
            : this(handle.createString(PARAMETERS[0], PARAMETERS[1], PARAMETERS[2], PARAMETERS[3], PARAMETERS[4], PARAMETERS[5], PARAMETERS[6], PARAMETERS[7], PARAMETERS[8], locale), 9)

    constructor(handle: LocalizedStringFactory10, locale: Locale)
            : this(handle.createString(PARAMETERS[0], PARAMETERS[1], PARAMETERS[2], PARAMETERS[3], PARAMETERS[4], PARAMETERS[5], PARAMETERS[6], PARAMETERS[7], PARAMETERS[8], PARAMETERS[9], locale), 10)

    fun toComponent(vararg parameters: Any, primaryColor: TextColor, secondaryColor: TextColor): Component {
        var component = Component.empty()
        var textToConvert = template

        var foundParameterPair = textToConvert.indexOfParameter()
        while (foundParameterPair != null) {
            val parameterValue = parameters[foundParameterPair.second]
            component = component
                .append(textToConvert.substring(0, foundParameterPair.first).toTextComponent(primaryColor))
                .append(parameterValue as? ComponentLike ?: parameterValue.toString().toParameterComponent(secondaryColor))

            textToConvert = textToConvert.substring(foundParameterPair.first + PARAMETER_LENGTH)
            foundParameterPair = textToConvert.indexOfParameter()
        }

        if (textToConvert.isNotEmpty()) {
            component = component.append(textToConvert.toTextComponent(primaryColor))
        }

        return component
    }

    private fun String.indexOfParameter() = parameters
        .map { parameter -> indexOf(parameter) to parameter }
        .filterNot { it.first == -1 }
        .map { it.first to PARAMETERS.indexOf(it.second) }
        .minByOrNull { it.first }
}

private fun String.toTextComponent(color: TextColor) = ComponentSerializer.deserialize(this).color(color)

private fun String.toParameterComponent(color: TextColor) = ComponentSerializer.deserialize(this.color()).color(color)