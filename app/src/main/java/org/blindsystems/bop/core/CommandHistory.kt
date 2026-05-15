package org.blindsystems.bop.core

/**
 * Command pattern for undo/redo of segment operations.
 * Mirrors core/commands.py.
 */
sealed class SegmentCommand {
    data class Add(val segment: Segment) : SegmentCommand()
    data class Remove(val segment: Segment, val originalIndex: Int) : SegmentCommand()
}

class CommandHistory {
    private val undoStack = ArrayDeque<SegmentCommand>()
    private val redoStack = ArrayDeque<SegmentCommand>()

    fun push(command: SegmentCommand) {
        undoStack.addLast(command)
        redoStack.clear()
    }

    fun canUndo() = undoStack.isNotEmpty()
    fun canRedo() = redoStack.isNotEmpty()

    fun undo(): SegmentCommand? {
        val cmd = undoStack.removeLastOrNull() ?: return null
        redoStack.addLast(cmd)
        return cmd
    }

    fun redo(): SegmentCommand? {
        val cmd = redoStack.removeLastOrNull() ?: return null
        undoStack.addLast(cmd)
        return cmd
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}
