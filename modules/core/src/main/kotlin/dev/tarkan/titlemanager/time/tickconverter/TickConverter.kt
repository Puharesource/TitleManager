package dev.tarkan.titlemanager.time.tickconverter

/**
 * An interface for converting between ticks and milliseconds.
 * Implementations define the specific logic for the conversion.
 */
interface TickConverter {
    /**
     * Converts the given time in milliseconds to ticks.
     *
     * @param milliseconds The time in milliseconds to be converted.
     * @return The equivalent time in ticks.
     */
    fun convertToTicks(milliseconds: UInt): UInt

    /**
     * Converts the given time in ticks to milliseconds.
     *
     * @param ticks The time in ticks to be converted.
     * @return The equivalent time in milliseconds.
     */
    fun convertToMilliseconds(ticks: UInt): UInt
}