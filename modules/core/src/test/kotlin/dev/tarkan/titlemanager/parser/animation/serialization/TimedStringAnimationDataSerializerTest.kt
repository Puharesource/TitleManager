package dev.tarkan.titlemanager.parser.animation.serialization

import dev.tarkan.titlemanager.time.Timing
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TimedStringAnimationDataSerializerTest {
    @Test
    fun `serializes null data as null`() {
        assertNull(TimedStringAnimationDataSerializer.serialize(null))
    }

    @Test
    fun `serializes text without timing unchanged`() {
        val data = TimedStringAnimationDataSerializer.Data(timing = null, text = "Hello")

        assertEquals("Hello", TimedStringAnimationDataSerializer.serialize(data))
    }

    @Test
    fun `serializes timing prefix before text`() {
        val data = TimedStringAnimationDataSerializer.Data(timing = Timing(1u, 2u, 3u), text = "Hello")

        assertEquals("[1;2;3]Hello", TimedStringAnimationDataSerializer.serialize(data))
    }

    @Test
    fun `deserializes null input as null`() {
        assertNull(TimedStringAnimationDataSerializer.deserialize(null))
    }

    @Test
    fun `deserializes text without timing`() {
        assertEquals(
            TimedStringAnimationDataSerializer.Data(timing = null, text = "Hello"),
            TimedStringAnimationDataSerializer.deserialize("Hello")
        )
    }

    @Test
    fun `deserializes timing prefix and text`() {
        assertEquals(
            TimedStringAnimationDataSerializer.Data(timing = Timing(1u, 2u, 3u), text = "Hello"),
            TimedStringAnimationDataSerializer.deserialize("[1;2;3]Hello")
        )
    }
}
