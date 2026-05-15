package org.blindsystems.bop.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SegmentManagerTest {

    private lateinit var manager: SegmentManager

    @BeforeEach
    fun setup() {
        manager = SegmentManager()
    }

    @Test
    fun testAddSegment() {
        val seg = Segment(name = "Test", start = 1000, end = 2000)
        manager.add(seg)
        assertEquals(1, manager.segments.size)
        assertEquals("Test", manager.segments[0].name)
    }

    @Test
    fun testAddMultipleSegments() {
        manager.add(Segment(name = "A", start = 0, end = 100))
        manager.add(Segment(name = "B", start = 100, end = 200))
        manager.add(Segment(name = "C", start = 200, end = 300))
        assertEquals(3, manager.segments.size)
    }

    @Test
    fun testRemoveExistingSegment() {
        val seg = Segment(name = "Test", start = 1000, end = 2000)
        manager.add(seg)
        assertTrue(manager.remove(seg.id))
        assertEquals(0, manager.segments.size)
    }

    @Test
    fun testRemoveNonExistentSegment() {
        assertFalse(manager.remove("non-existent-id"))
    }

    @Test
    fun testRemoveFromMultiple() {
        val seg1 = Segment(name = "A", start = 0, end = 100)
        val seg2 = Segment(name = "B", start = 100, end = 200)
        manager.add(seg1)
        manager.add(seg2)
        assertTrue(manager.remove(seg1.id))
        assertEquals(1, manager.segments.size)
        assertEquals("B", manager.segments[0].name)
    }

    @Test
    fun testMoveUp() {
        val seg1 = Segment(name = "A", start = 1000, end = 2000)
        val seg2 = Segment(name = "B", start = 2000, end = 3000)
        manager.add(seg1)
        manager.add(seg2)

        assertTrue(manager.moveUp(seg2.id))
        assertEquals("B", manager.segments[0].name)
        assertEquals("A", manager.segments[1].name)
    }

    @Test
    fun testMoveUpFirstElement() {
        val seg = Segment(name = "A", start = 0, end = 100)
        manager.add(seg)
        assertFalse(manager.moveUp(seg.id))
    }

    @Test
    fun testMoveUpNonExistent() {
        assertFalse(manager.moveUp("non-existent"))
    }

    @Test
    fun testMoveDown() {
        val seg1 = Segment(name = "A", start = 1000, end = 2000)
        val seg2 = Segment(name = "B", start = 2000, end = 3000)
        manager.add(seg1)
        manager.add(seg2)

        assertTrue(manager.moveDown(seg1.id))
        assertEquals("B", manager.segments[0].name)
        assertEquals("A", manager.segments[1].name)
    }

    @Test
    fun testMoveDownLastElement() {
        val seg = Segment(name = "A", start = 0, end = 100)
        manager.add(seg)
        assertFalse(manager.moveDown(seg.id))
    }

    @Test
    fun testMoveDownNonExistent() {
        assertFalse(manager.moveDown("non-existent"))
    }

    @Test
    fun testGetByIdFound() {
        val seg = Segment(name = "FindMe", start = 0, end = 100)
        manager.add(seg)
        val found = manager.getById(seg.id)
        assertNotNull(found)
        assertEquals("FindMe", found!!.name)
    }

    @Test
    fun testGetByIdNotFound() {
        assertNull(manager.getById("non-existent"))
    }

    @Test
    fun testSetAll() {
        manager.add(Segment(name = "Old", start = 0, end = 100))
        val newList = listOf(
            Segment(name = "New1", start = 0, end = 50),
            Segment(name = "New2", start = 50, end = 100)
        )
        manager.setAll(newList)
        assertEquals(2, manager.segments.size)
        assertEquals("New1", manager.segments[0].name)
        assertEquals("New2", manager.segments[1].name)
    }

    @Test
    fun testClear() {
        manager.add(Segment(name = "A", start = 0, end = 100))
        manager.add(Segment(name = "B", start = 100, end = 200))
        manager.clear()
        assertEquals(0, manager.segments.size)
    }

    @Test
    fun testSegmentsReturnsImmutableCopy() {
        val seg = Segment(name = "A", start = 0, end = 100)
        manager.add(seg)
        val list1 = manager.segments
        manager.add(Segment(name = "B", start = 100, end = 200))
        val list2 = manager.segments
        // list1 should not have changed
        assertEquals(1, list1.size)
        assertEquals(2, list2.size)
    }
}
