package org.blindsystems.bop.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class SegmentTest {

    @Test
    fun testDuration() {
        val seg = Segment(name = "Test", start = 1000, end = 2500)
        assertEquals(1500L, seg.durationMs)
    }

    @Test
    fun testDefaultValues() {
        val seg = Segment(name = "Test", start = 0, end = 100)
        assertNotNull(seg.id)
        assertEquals("", seg.category)
        assertEquals("#4FC3F7", seg.color)
        assertEquals("", seg.notes)
    }

    @Test
    fun testCustomValues() {
        val seg = Segment(
            id = "custom-id",
            name = "Custom",
            start = 500,
            end = 1500,
            category = "Guitar",
            color = "#FF0000",
            notes = "Difficult passage"
        )
        assertEquals("custom-id", seg.id)
        assertEquals("Custom", seg.name)
        assertEquals(500L, seg.start)
        assertEquals(1500L, seg.end)
        assertEquals("Guitar", seg.category)
        assertEquals("#FF0000", seg.color)
        assertEquals("Difficult passage", seg.notes)
        assertEquals(1000L, seg.durationMs)
    }

    @Test
    fun testUniqueIds() {
        val seg1 = Segment(name = "A", start = 0, end = 100)
        val seg2 = Segment(name = "B", start = 0, end = 100)
        assertNotEquals(seg1.id, seg2.id)
    }

    @Test
    fun testZeroDuration() {
        val seg = Segment(name = "Zero", start = 100, end = 100)
        assertEquals(0L, seg.durationMs)
    }

    @Test
    fun testCopy() {
        val seg = Segment(name = "Original", start = 0, end = 100)
        val copy = seg.copy(name = "Copy")
        assertEquals("Copy", copy.name)
        assertEquals(seg.id, copy.id)
        assertEquals(seg.start, copy.start)
    }
}
