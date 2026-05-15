package org.blindsystems.bop.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CommandHistoryTest {

    private lateinit var history: CommandHistory

    @BeforeEach
    fun setup() {
        history = CommandHistory()
    }

    @Test
    fun testInitialState() {
        assertFalse(history.canUndo())
        assertFalse(history.canRedo())
    }

    @Test
    fun testPushAndUndo() {
        val seg = Segment(name = "Test", start = 0, end = 100)
        val cmd = SegmentCommand.Add(seg)

        history.push(cmd)
        assertTrue(history.canUndo())
        assertFalse(history.canRedo())

        val undone = history.undo()
        assertEquals(cmd, undone)
        assertFalse(history.canUndo())
        assertTrue(history.canRedo())
    }

    @Test
    fun testRedo() {
        val seg = Segment(name = "Test", start = 0, end = 100)
        val cmd = SegmentCommand.Add(seg)

        history.push(cmd)
        history.undo()

        val redone = history.redo()
        assertEquals(cmd, redone)
        assertTrue(history.canUndo())
        assertFalse(history.canRedo())
    }

    @Test
    fun testUndoEmpty() {
        assertNull(history.undo())
    }

    @Test
    fun testRedoEmpty() {
        assertNull(history.redo())
    }

    @Test
    fun testClear() {
        history.push(SegmentCommand.Add(Segment(name = "A", start = 0, end = 10)))
        history.clear()
        assertFalse(history.canUndo())
        assertFalse(history.canRedo())
    }

    @Test
    fun testRedoStackClearedOnPush() {
        history.push(SegmentCommand.Add(Segment(name = "A", start = 0, end = 10)))
        history.undo()
        assertTrue(history.canRedo())

        history.push(SegmentCommand.Add(Segment(name = "B", start = 0, end = 10)))
        assertFalse(history.canRedo())
    }

    @Test
    fun testMultipleUndoRedo() {
        val cmd1 = SegmentCommand.Add(Segment(name = "A", start = 0, end = 10))
        val cmd2 = SegmentCommand.Add(Segment(name = "B", start = 10, end = 20))
        val cmd3 = SegmentCommand.Remove(Segment(name = "C", start = 20, end = 30), 0)

        history.push(cmd1)
        history.push(cmd2)
        history.push(cmd3)

        assertEquals(cmd3, history.undo())
        assertEquals(cmd2, history.undo())
        assertEquals(cmd1, history.undo())
        assertNull(history.undo())

        assertEquals(cmd1, history.redo())
        assertEquals(cmd2, history.redo())
        assertEquals(cmd3, history.redo())
        assertNull(history.redo())
    }

    @Test
    fun testSegmentCommandAddData() {
        val seg = Segment(name = "A", start = 0, end = 100)
        val cmd = SegmentCommand.Add(seg)
        assertEquals(seg, cmd.segment)
    }

    @Test
    fun testSegmentCommandRemoveData() {
        val seg = Segment(name = "A", start = 0, end = 100)
        val cmd = SegmentCommand.Remove(seg, 2)
        assertEquals(seg, cmd.segment)
        assertEquals(2, cmd.originalIndex)
    }

    @Test
    fun testClearAfterUndoRedo() {
        history.push(SegmentCommand.Add(Segment(name = "A", start = 0, end = 10)))
        history.undo()
        history.clear()
        assertFalse(history.canUndo())
        assertFalse(history.canRedo())
    }
}
