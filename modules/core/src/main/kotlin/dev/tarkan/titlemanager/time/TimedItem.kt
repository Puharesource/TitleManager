package dev.tarkan.titlemanager.time

/**
 * Represents an item associated with a specific [Timing].
 *
 * @param TItem The type of the item being timed.
 * @property timing The [Timing] associated with the item, which defines its fade-in, stay, and fade-out durations.
 * @property item The item associated with the timing.
 */
data class TimedItem<TItem>(val timing: Timing, val item: TItem)