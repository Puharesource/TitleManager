package dev.tarkan.titlemanager.parser

import dev.tarkan.titlemanager.time.Timing

/**
 * Parses animation files or strings to extract lines with associated timings.
 *
 * @property timingScale A scaling factor applied to all parsed timings. Defaults to `1u`.
 */
class IntermediaryParser(private val timingScale: UInt = 1u) {
    private companion object {
        /**
         * Regex for parsing lines with fade-in, stay, and fade-out timings.
         */
        private val FULL_LINE_REGEX: Regex = """^\[(\d+);(\d+);(\d+)\](.*)$""".toRegex()

        /**
         * Regex for parsing lines with only stay timing.
         */
        private val SIMPLE_LINE_REGEX: Regex = """^\[(\d+)\](.*)$""".toRegex()
    }

    /**
     * Parses text containing animation lines.
     *
     * @param text The text to parse.
     * @return A list of [IntermediaryParsedAnimationLine] representing the parsed animation lines.
     */
    fun parseText(text: String): List<IntermediaryParsedAnimationLine> {
        if (text.isEmpty()) {
            return emptyList()
        }

        val normalizedText = text.replace("\r\n", "\n").replace('\r', '\n')
        val lines = normalizedText.split('\n').let {
            if (normalizedText.endsWith('\n')) it.dropLast(1) else it
        }

        return parseLines(lines)
    }

    /**
     * Parses lines containing animation text.
     *
     * @param lines The lines to parse.
     * @return A list of [IntermediaryParsedAnimationLine] representing the parsed animation lines.
     */
    fun parseLines(lines: Iterable<String>): List<IntermediaryParsedAnimationLine> {
        val parsedLines = mutableListOf<IntermediaryParsedAnimationLine>()
        var previousTiming = Timing.instant

        for (line in lines) {
            val parsedLine = parseLine(line, previousTiming)
            parsedLines.add(parsedLine)

            previousTiming = parsedLine.timing
        }

        return parsedLines
    }

    /**
     * Parses a single line of animation text, using the previous line's timing if none is provided.
     *
     * @param line The line to parse.
     * @param previousTiming The timing to use if the line does not specify one.
     * @return A [IntermediaryParsedAnimationLine] representing the parsed line.
     */
    fun parseLine(line: String, previousTiming: Timing = Timing.default): IntermediaryParsedAnimationLine {
        var matchResult = FULL_LINE_REGEX.matchEntire(line)
        if (matchResult != null) {
            return parseFullLineRegex(matchResult)
        }

        matchResult = SIMPLE_LINE_REGEX.matchEntire(line)
        if (matchResult != null) {
            return parseSimpleLineRegex(matchResult)
        }

        return IntermediaryParsedAnimationLine(previousTiming, line)
    }

    /**
     * Parses a line matching the full timing regex.
     *
     * @param matchResult The match result from [FULL_LINE_REGEX].
     * @return A [IntermediaryParsedAnimationLine] containing the parsed timing and text.
     */
    private fun parseFullLineRegex(matchResult: MatchResult): IntermediaryParsedAnimationLine {
        val fadeIn = matchResult.groups[1]!!.value.toUInt()
        val stay = matchResult.groups[2]!!.value.toUInt()
        val fadeOut = matchResult.groups[3]!!.value.toUInt()
        val text = matchResult.groups[4]!!.value

        val timing = Timing(fadeIn, stay, fadeOut) * timingScale

        return IntermediaryParsedAnimationLine(timing, text)
    }

    /**
     * Parses a line matching the simple timing regex.
     *
     * @param matchResult The match result from [SIMPLE_LINE_REGEX].
     * @return A [IntermediaryParsedAnimationLine] containing the parsed timing and text.
     */
    private fun parseSimpleLineRegex(matchResult: MatchResult): IntermediaryParsedAnimationLine {
        val stay = matchResult.groups[1]!!.value.toUInt()
        val text = matchResult.groups[2]!!.value

        val timing = Timing.createStatic(stay, timingScale)

        return IntermediaryParsedAnimationLine(timing, text)
    }
}