@file:OptIn(ExperimentalJsExport::class)

package dev.tarkan.titlemanager.api

import dev.tarkan.titlemanager.animation.TimelineAnimation
import dev.tarkan.titlemanager.parser.IntermediaryAnimationPlaceholderNode
import dev.tarkan.titlemanager.parser.IntermediaryParsedAnimationLine
import dev.tarkan.titlemanager.parser.IntermediaryParser
import dev.tarkan.titlemanager.parser.IntermediaryTextNode
import dev.tarkan.titlemanager.parser.IntermediaryVariablePlaceholderNode
import dev.tarkan.titlemanager.parser.animation.AnimationParser
import dev.tarkan.titlemanager.parser.placeholder.animation.AnimationPlaceholderRegistry
import dev.tarkan.titlemanager.parser.placeholder.animation.addCoreAnimationPlaceholders
import dev.tarkan.titlemanager.parser.placeholder.variable.VariablePlaceholderRegistry
import dev.tarkan.titlemanager.time.Timing
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.math.floor

@JsExport
class TitleManagerParseResult(
    val lines: Array<TitleManagerParsedLine>,
    val errors: Array<TitleManagerParseError>
) {
    val isSuccess: Boolean = errors.isEmpty()
}

@JsExport
class TitleManagerParsedLine(
    val lineNumber: Int,
    val fadeInMilliseconds: Double,
    val stayMilliseconds: Double,
    val fadeOutMilliseconds: Double,
    val totalMilliseconds: Double,
    val text: String,
    val nodes: Array<TitleManagerParsedNode>
)

@JsExport
class TitleManagerParsedNode(
    val type: String,
    val text: String,
    val id: String,
    val data: String?
)

@JsExport
class TitleManagerParseError(
    val lineNumber: Int,
    val line: String,
    val message: String
)

@JsExport
class TitleManagerTimelineResult(
    val frames: Array<TitleManagerTimelineFrame>,
    val errors: Array<TitleManagerParseError>,
    val isInfinite: Boolean,
    val totalMilliseconds: Double?
) {
    val isSuccess: Boolean = errors.isEmpty()
}

@JsExport
class TitleManagerTimelineFrame(
    val frameNumber: Int,
    val startMilliseconds: Double,
    val endMilliseconds: Double?,
    val fadeInMilliseconds: Double,
    val stayMilliseconds: Double,
    val fadeOutMilliseconds: Double,
    val totalMilliseconds: Double,
    val text: String
)

@JsExport
class TitleManagerLegacyTextSegment(
    val text: String,
    val color: String?,
    val bold: Boolean,
    val italic: Boolean,
    val underlined: Boolean,
    val strikethrough: Boolean,
    val obfuscated: Boolean
)

@JsExport
object TitleManagerCoreApi {
    private const val DEFAULT_MAX_INPUT_CHARACTERS = 10_000
    private const val DEFAULT_MAX_LINES = 500
    private const val DEFAULT_MAX_FRAMES = 2_000
    private const val DEFAULT_MAX_OUTPUT_CHARACTERS = 100_000

    @JsName("parseAnimationText")
    fun parseAnimationText(text: String): TitleManagerParseResult {
        return parseAnimationTextWithTimingScale(text, 1)
    }

    @JsName("parseAnimationTextWithTimingScale")
    fun parseAnimationTextWithTimingScale(text: String, timingScale: Int): TitleManagerParseResult {
        val parsedText = parseAnimationTextInternal(text, timingScale)

        return TitleManagerParseResult(parsedText.lines, parsedText.errors)
    }

    @JsName("createAnimationTimeline")
    fun createAnimationTimeline(text: String): TitleManagerTimelineResult {
        return createAnimationTimelineWithTimingScale(text, 1)
    }

    @JsName("createAnimationTimelineWithTimingScale")
    fun createAnimationTimelineWithTimingScale(text: String, timingScale: Int): TitleManagerTimelineResult {
        return createAnimationTimelineWithSafetyLimits(
            text,
            timingScale,
            DEFAULT_MAX_INPUT_CHARACTERS,
            DEFAULT_MAX_LINES,
            DEFAULT_MAX_FRAMES,
            DEFAULT_MAX_OUTPUT_CHARACTERS
        )
    }

    @JsName("createAnimationTimelineWithSafetyLimits")
    fun createAnimationTimelineWithSafetyLimits(
        text: String,
        timingScale: Int,
        maxInputCharacters: Int,
        maxLines: Int,
        maxFrames: Int,
        maxOutputCharacters: Int
    ): TitleManagerTimelineResult {
        val limits = SafetyLimits(
            maxInputCharacters = maxInputCharacters,
            maxLines = maxLines,
            maxFrames = maxFrames,
            maxOutputCharacters = maxOutputCharacters
        )
        val limitValidationError = limits.validate()
            ?: validateInputLimits(text, limits)

        if (limitValidationError != null) {
            return TitleManagerTimelineResult(emptyArray(), arrayOf(limitValidationError), false, 0.0)
        }

        val parsedText = parseAnimationTextInternal(text, timingScale)

        if (parsedText.errors.isNotEmpty()) {
            return TitleManagerTimelineResult(emptyArray(), parsedText.errors, false, 0.0)
        }

        val timeline = try {
            val animationParser = AnimationParser(
                buildVariablePlaceholderRegistry(parsedText.parsedLines),
                buildAnimationPlaceholderRegistry()
            )
            val animation = animationParser.parseAnimation(parsedText.parsedLines.toList())
            val timelineAnimation = animation as? TimelineAnimation<Unit, String>
                ?: return TitleManagerTimelineResult(
                    emptyArray(),
                    arrayOf(TitleManagerParseError(0, "", "Animation does not expose deterministic timeline frames")),
                    false,
                    0.0
                )

            timelineAnimation.singleIterationTimeline(Unit)
        } catch (exception: IllegalArgumentException) {
            return TitleManagerTimelineResult(
                emptyArray(),
                arrayOf(TitleManagerParseError(0, "", exception.message ?: "Invalid animation timeline")),
                false,
                0.0
            )
        }

        if (timeline.frames.size > limits.maxFrames) {
            return TitleManagerTimelineResult(
                emptyArray(),
                arrayOf(TitleManagerParseError(0, "", "Generated timeline has ${timeline.frames.size} frames, exceeding maxFrames ${limits.maxFrames}")),
                timeline.isInfinite,
                timeline.totalMilliseconds?.toDouble()
            )
        }

        val outputCharacters = timeline.frames.sumOf { frame -> frame.item.length }
        if (outputCharacters > limits.maxOutputCharacters) {
            return TitleManagerTimelineResult(
                emptyArray(),
                arrayOf(TitleManagerParseError(0, "", "Generated timeline has $outputCharacters output characters, exceeding maxOutputCharacters ${limits.maxOutputCharacters}")),
                timeline.isInfinite,
                timeline.totalMilliseconds?.toDouble()
            )
        }

        val frames = timeline.frames.mapIndexed { index, frame ->
            TitleManagerTimelineFrame(
                frameNumber = index + 1,
                startMilliseconds = frame.startMilliseconds.toDouble(),
                endMilliseconds = frame.endMilliseconds?.toDouble(),
                fadeInMilliseconds = frame.timing.fadeInMilliseconds.toDouble(),
                stayMilliseconds = frame.timing.stayMilliseconds.toDouble(),
                fadeOutMilliseconds = frame.timing.fadeOutMilliseconds.toDouble(),
                totalMilliseconds = frame.timing.totalMilliseconds.toDouble(),
                text = frame.item
            )
        }

        return TitleManagerTimelineResult(
            frames.toTypedArray(),
            emptyArray(),
            timeline.isInfinite,
            timeline.totalMilliseconds?.toDouble()
        )
    }

    @JsName("renderLegacyText")
    fun renderLegacyText(text: String): Array<TitleManagerLegacyTextSegment> {
        return LegacyTextRenderer.render(text)
    }

    @JsName("translateLegacyColorCodes")
    fun translateLegacyColorCodes(text: String): String {
        return LegacyTextRenderer.translateAlternateColorCodes(text)
    }

    @JsName("legacyColorCode")
    fun legacyColorCode(color: String): String {
        return LegacyTextRenderer.legacyColorCode(color)
    }

    @JsName("legacyRgbColorCode")
    fun legacyRgbColorCode(red: Int, green: Int, blue: Int): String {
        return LegacyTextRenderer.legacyRgbColorCode(red, green, blue)
    }

    @JsName("legacyRgbIntColorCode")
    fun legacyRgbIntColorCode(rgb: Int): String {
        return LegacyTextRenderer.legacyRgbIntColorCode(rgb)
    }

    @JsName("parseLegacyHexColorRgb")
    fun parseLegacyHexColorRgb(color: String): Int {
        return LegacyTextRenderer.parseLegacyHexColorRgb(color)
    }

    @JsName("formatLegacyGradient")
    fun formatLegacyGradient(data: String?): String {
        return LegacyTextRenderer.formatLegacyGradient(data)
    }

    @JsName("splitTypedLineBreak")
    fun splitTypedLineBreak(text: String, limit: Int): Array<String> {
        return text.splitTypedLineBreak(limit).toTypedArray()
    }

    private fun parseAnimationTextInternal(text: String, timingScale: Int): ParsedAnimationText {
        if (timingScale < 1) {
            return ParsedAnimationText(
                emptyArray(),
                emptyArray(),
                arrayOf(TitleManagerParseError(0, "", "timingScale must be at least 1"))
            )
        }

        val unsignedTimingScale = timingScale.toUInt()
        val parser = IntermediaryParser(unsignedTimingScale)
        val intermediaryLines = mutableListOf<IntermediaryParsedAnimationLine>()
        val parsedLines = mutableListOf<TitleManagerParsedLine>()
        val errors = mutableListOf<TitleManagerParseError>()
        var previousTiming = Timing.instant

        for ((lineIndex, line) in text.animationLines().withIndex()) {
            try {
                val parsedLine = parser.parseLine(line, previousTiming)

                intermediaryLines.add(parsedLine)
                parsedLines.add(
                    TitleManagerParsedLine(
                        lineNumber = lineIndex + 1,
                        fadeInMilliseconds = parsedLine.timing.fadeInMilliseconds.toDouble(),
                        stayMilliseconds = parsedLine.timing.stayMilliseconds.toDouble(),
                        fadeOutMilliseconds = parsedLine.timing.fadeOutMilliseconds.toDouble(),
                        totalMilliseconds = parsedLine.timing.totalMilliseconds.toDouble(),
                        text = parsedLine.text,
                        nodes = parsedLine.parse().map { node ->
                            when (node) {
                                is IntermediaryTextNode -> TitleManagerParsedNode(
                                    type = "text",
                                    text = node.text,
                                    id = "",
                                    data = null
                                )

                                is IntermediaryAnimationPlaceholderNode -> TitleManagerParsedNode(
                                    type = "animation",
                                    text = "",
                                    id = node.id,
                                    data = node.data
                                )

                                is IntermediaryVariablePlaceholderNode -> TitleManagerParsedNode(
                                    type = "variable",
                                    text = "",
                                    id = node.id,
                                    data = node.data
                                )

                                else -> TitleManagerParsedNode(
                                    type = "unknown",
                                    text = "",
                                    id = "",
                                    data = null
                                )
                            }
                        }.toTypedArray()
                    )
                )

                previousTiming = parsedLine.timing
            } catch (exception: IllegalArgumentException) {
                errors.add(
                    TitleManagerParseError(
                        lineNumber = lineIndex + 1,
                        line = line,
                        message = exception.message ?: "Invalid animation line"
                    )
                )
            }
        }

        return ParsedAnimationText(
            intermediaryLines.toTypedArray(),
            parsedLines.toTypedArray(),
            errors.toTypedArray()
        )
    }
}

private class SafetyLimits(
    val maxInputCharacters: Int,
    val maxLines: Int,
    val maxFrames: Int,
    val maxOutputCharacters: Int
) {
    fun validate(): TitleManagerParseError? {
        val invalidLimit = listOf(
            "maxInputCharacters" to maxInputCharacters,
            "maxLines" to maxLines,
            "maxFrames" to maxFrames,
            "maxOutputCharacters" to maxOutputCharacters
        ).firstOrNull { (_, value) -> value < 1 }

        return invalidLimit?.let { (name, _) ->
            TitleManagerParseError(0, "", "$name must be at least 1")
        }
    }
}

private fun validateInputLimits(text: String, limits: SafetyLimits): TitleManagerParseError? {
    if (text.length > limits.maxInputCharacters) {
        return TitleManagerParseError(
            0,
            "",
            "Input has ${text.length} characters, exceeding maxInputCharacters ${limits.maxInputCharacters}"
        )
    }

    val lineCount = text.animationLines().size
    if (lineCount > limits.maxLines) {
        return TitleManagerParseError(
            0,
            "",
            "Input has $lineCount lines, exceeding maxLines ${limits.maxLines}"
        )
    }

    return null
}

private object LegacyTextRenderer {
    private val legacyColors = mapOf(
        '0' to "#000000",
        '1' to "#0000aa",
        '2' to "#00aa00",
        '3' to "#00aaaa",
        '4' to "#aa0000",
        '5' to "#aa00aa",
        '6' to "#ffaa00",
        '7' to "#aaaaaa",
        '8' to "#555555",
        '9' to "#5555ff",
        'a' to "#55ff55",
        'b' to "#55ffff",
        'c' to "#ff5555",
        'd' to "#ff55ff",
        'e' to "#ffff55",
        'f' to "#ffffff"
    )

    fun render(text: String): Array<TitleManagerLegacyTextSegment> {
        val segments = mutableListOf<TitleManagerLegacyTextSegment>()
        val currentText = StringBuilder()
        val style = LegacyTextStyle()
        var index = 0

        fun flush() {
            if (currentText.isEmpty()) {
                return
            }

            segments += style.segment(currentText.toString())
            currentText.clear()
        }

        while (index < text.length) {
            val char = text[index]
            if (!char.isLegacyControlChar() || index + 1 >= text.length) {
                currentText.append(char)
                index++
                continue
            }

            val code = text[index + 1].lowercaseChar()
            val hexColor = text.readLegacyHexColor(index)
            when {
                hexColor != null -> {
                    flush()
                    style.color = hexColor
                    style.resetFormats()
                    index += 14
                }

                code in legacyColors -> {
                    flush()
                    style.color = legacyColors.getValue(code)
                    style.resetFormats()
                    index += 2
                }

                code == 'r' -> {
                    flush()
                    style.reset()
                    index += 2
                }

                code == 'l' -> {
                    flush()
                    style.bold = true
                    index += 2
                }

                code == 'o' -> {
                    flush()
                    style.italic = true
                    index += 2
                }

                code == 'n' -> {
                    flush()
                    style.underlined = true
                    index += 2
                }

                code == 'm' -> {
                    flush()
                    style.strikethrough = true
                    index += 2
                }

                code == 'k' -> {
                    flush()
                    style.obfuscated = true
                    index += 2
                }

                else -> {
                    currentText.append(char)
                    index++
                }
            }
        }

        flush()
        return segments.toTypedArray()
    }

    fun translateAlternateColorCodes(text: String): String {
        val translated = StringBuilder(text.length)
        var index = 0

        while (index < text.length) {
            val char = text[index]
            if (char != '&' || index + 1 >= text.length) {
                translated.append(char)
                index++
                continue
            }

            val hexColor = text.readLegacyHexColor(index)
            if (hexColor != null) {
                translated.append('§').append('x')
                hexColor.drop(1).forEach { digit ->
                    translated.append('§').append(digit)
                }
                index += 14
                continue
            }

            val code = text[index + 1].lowercaseChar()
            if (code in legacyColors || code in legacyFormatCodes) {
                translated.append('§').append(code)
                index += 2
            } else {
                translated.append(char)
                index++
            }
        }

        return translated.toString()
    }

    fun legacyColorCode(color: String): String = encodeLegacyHexColor(parseHexColor(color))

    fun legacyRgbColorCode(red: Int, green: Int, blue: Int): String {
        return encodeLegacyHexColor(RgbColor(red, green, blue))
    }

    fun legacyRgbIntColorCode(rgb: Int): String {
        return encodeLegacyHexColor(RgbColor.fromRgbInt(rgb))
    }

    fun parseLegacyHexColorRgb(color: String): Int = parseHexColor(color).toRgbInt()

    fun formatLegacyGradient(data: String?): String {
        if (data == null) {
            return "N/A"
        }

        val match = legacyGradientRegex.matchEntire(data)
        val text = match?.groups?.get(2)?.value ?: data
        val colorAndFormatData = match?.groups?.get(1)?.value
        var bold = false
        var strikethrough = false
        var underline = false
        var magic = false
        val colors = colorAndFormatData
            ?.split(",")
            ?.asSequence()
            ?.map { it.trim() }
            ?.onEach {
                when {
                    it.equals("bold", ignoreCase = true) -> bold = true
                    it.equals("strikethrough", ignoreCase = true) -> strikethrough = true
                    it.equals("underline", ignoreCase = true) -> underline = true
                    it.equals("magic", ignoreCase = true) -> magic = true
                }
            }
            ?.filter { it.startsWith("#") }
            ?.map(::parseHexColor)
            ?.toList()
            ?: listOf(RgbColor(255, 0, 0), RgbColor(0, 255, 0))

        require(colors.isNotEmpty()) { "Parameter 'colors' cannot be empty" }

        return buildString {
            for (i in text.indices) {
                val percentage = i.toFloat() / text.length.toFloat()
                append(encodeLegacyHexColor(colors.interpolateLegacyGradientColor(percentage)))

                if (bold) append("§l")
                if (strikethrough) append("§m")
                if (underline) append("§n")
                if (magic) append("§k")

                append(text[i])
            }
        }
    }

    private val legacyFormatCodes = setOf('k', 'l', 'm', 'n', 'o', 'r')
    private val legacyGradientRegex = """^\[(.+)\](.+)$""".toRegex()

    private fun Char.isLegacyControlChar() = this == '§' || this == '&'

    private fun encodeLegacyHexColor(color: RgbColor): String {
        return buildString(14) {
            append('§').append('x')
            color.toHexString().forEach { digit ->
                append('§').append(digit)
            }
        }
    }

    private fun parseHexColor(color: String): RgbColor {
        require(color.length == 7 && color.startsWith("#")) {
            "Legacy RGB colors must use #RRGGBB syntax"
        }

        val red = color.substring(1, 3).toIntOrNull(16)
        val green = color.substring(3, 5).toIntOrNull(16)
        val blue = color.substring(5, 7).toIntOrNull(16)
        require(red != null && green != null && blue != null) {
            "Legacy RGB colors must use #RRGGBB syntax"
        }

        return RgbColor(red, green, blue)
    }

    private fun List<RgbColor>.interpolateLegacyGradientColor(percentage: Float): RgbColor {
        if (size == 1) {
            return first()
        }

        if (size == 2) {
            return first().interpolate(this[1], percentage)
        }

        val startIndex = floor((size - 1) * percentage).toInt()
        val interpolatorPercentage = ((size - 1) * percentage) % 1.0f

        return this[startIndex].interpolate(this[startIndex + 1], interpolatorPercentage)
    }

    private fun String.readLegacyHexColor(index: Int): String? {
        if (index + 13 >= length || this[index + 1].lowercaseChar() != 'x') {
            return null
        }

        val hex = StringBuilder("#")
        var cursor = index + 2
        repeat(6) {
            if (!this[cursor].isLegacyControlChar()) {
                return null
            }

            val digit = this[cursor + 1].lowercaseChar()
            if (digit !in '0'..'9' && digit !in 'a'..'f') {
                return null
            }

            hex.append(digit)
            cursor += 2
        }

        return hex.toString()
    }
}

private data class RgbColor(
    val red: Int,
    val green: Int,
    val blue: Int
) {
    companion object {
        fun fromRgbInt(rgb: Int): RgbColor {
            return RgbColor(
                red = (rgb shr 16) and 0xff,
                green = (rgb shr 8) and 0xff,
                blue = rgb and 0xff
            )
        }
    }

    init {
        require(red in 0..255 && green in 0..255 && blue in 0..255) {
            "RGB channels must be between 0 and 255"
        }
    }

    fun toHexString(): String {
        return listOf(red, green, blue).joinToString("") { channel ->
            channel.toString(16).padStart(2, '0')
        }
    }

    fun toRgbInt(): Int = (red shl 16) or (green shl 8) or blue

    fun interpolate(end: RgbColor, percentage: Float): RgbColor {
        return RgbColor(
            floor(red * (1.0f - percentage) + end.red * percentage).toInt(),
            floor(green * (1.0f - percentage) + end.green * percentage).toInt(),
            floor(blue * (1.0f - percentage) + end.blue * percentage).toInt()
        )
    }
}

private class LegacyTextStyle {
    var color: String? = null
    var bold: Boolean = false
    var italic: Boolean = false
    var underlined: Boolean = false
    var strikethrough: Boolean = false
    var obfuscated: Boolean = false

    fun reset() {
        color = null
        resetFormats()
    }

    fun resetFormats() {
        bold = false
        italic = false
        underlined = false
        strikethrough = false
        obfuscated = false
    }

    fun segment(text: String) = TitleManagerLegacyTextSegment(
        text = text,
        color = color,
        bold = bold,
        italic = italic,
        underlined = underlined,
        strikethrough = strikethrough,
        obfuscated = obfuscated
    )
}

private val typedLineBreakMarkers = arrayOf("%nl%", "<nl>", "{nl}", "\\n", "\n")

private fun String.splitTypedLineBreak(limit: Int = 0): List<String> {
    require(limit >= 0) { "Limit must be non-negative, but was $limit" }

    val result = mutableListOf<String>()
    var cursor = 0

    while (cursor <= length) {
        if (limit > 0 && result.size == limit - 1) {
            result += substring(cursor)
            return result
        }

        val next = typedLineBreakMarkers
            .asSequence()
            .mapNotNull { marker ->
                val index = indexOf(marker, cursor)
                if (index >= 0) index to marker.length else null
            }
            .minByOrNull { it.first }

        if (next == null) {
            result += substring(cursor)
            break
        }

        result += substring(cursor, next.first)
        cursor = next.first + next.second
    }

    if (limit == 0) {
        while (result.lastOrNull() == "") {
            result.removeAt(result.lastIndex)
        }
    }

    return result
}

private class ParsedAnimationText(
    val parsedLines: Array<IntermediaryParsedAnimationLine>,
    val lines: Array<TitleManagerParsedLine>,
    val errors: Array<TitleManagerParseError>
)

private fun buildVariablePlaceholderRegistry(
    parsedLines: Array<IntermediaryParsedAnimationLine>
): VariablePlaceholderRegistry<Unit> {
    val variableIds = parsedLines
        .flatMap { line -> line.parse().filterIsInstance<IntermediaryVariablePlaceholderNode>() }
        .map { it.id }
        .distinctBy { it.lowercase() }

    return VariablePlaceholderRegistry.build {
        for (id in variableIds) {
            val placeholderId = id
            addSimple(placeholderId) { data ->
                if (data == null) "%{$placeholderId}" else "%{$placeholderId:$data}"
            }
        }
    }
}

private fun buildAnimationPlaceholderRegistry(): AnimationPlaceholderRegistry<Unit> =
    AnimationPlaceholderRegistry.build {
        addCoreAnimationPlaceholders()
    }

private fun String.animationLines(): List<String> {
    if (isEmpty()) {
        return emptyList()
    }

    val normalizedText = replace("\r\n", "\n").replace('\r', '\n')
    val lines = normalizedText.split('\n')

    return if (normalizedText.endsWith('\n')) lines.dropLast(1) else lines
}
