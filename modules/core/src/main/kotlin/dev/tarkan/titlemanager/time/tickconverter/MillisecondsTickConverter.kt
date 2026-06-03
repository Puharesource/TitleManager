package dev.tarkan.titlemanager.time.tickconverter

/**
 * A converter for ticks and milliseconds, where 1 tick equals 1 millisecond.
 * This implementation assumes a direct one-to-one mapping between ticks and milliseconds.
 */
object MillisecondsTickConverter : TickConverter {
    /**
     * Converts the given time in milliseconds to ticks.
     * In this implementation, 1 millisecond equals 1 tick.
     *
     * @param milliseconds The time in milliseconds to be converted.
     * @return The equivalent time in ticks.
     */
    override fun convertToTicks(milliseconds: UInt) = milliseconds

    /**
     * Converts the given time in ticks to milliseconds.
     * In this implementation, 1 tick equals 1 millisecond.
     *
     * @param ticks The time in ticks to be converted.
     * @return The equivalent time in milliseconds.
     */
    override fun convertToMilliseconds(ticks: UInt) = ticks
}